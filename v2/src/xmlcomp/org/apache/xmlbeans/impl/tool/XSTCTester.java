/*
* The Apache Software License, Version 1.1
*
*
* Copyright (c) 2003 The Apache Software Foundation.  All rights 
* reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
*
* 1. Redistributions of source code must retain the above copyright
*    notice, this list of conditions and the following disclaimer. 
*
* 2. Redistributions in binary form must reproduce the above copyright
*    notice, this list of conditions and the following disclaimer in
*    the documentation and/or other materials provided with the
*    distribution.
*
* 3. The end-user documentation included with the redistribution,
*    if any, must include the following acknowledgment:  
*       "This product includes software developed by the
*        Apache Software Foundation (http://www.apache.org/)."
*    Alternately, this acknowledgment may appear in the software itself,
*    if and wherever such third-party acknowledgments normally appear.
*
* 4. The names "Apache" and "Apache Software Foundation" must 
*    not be used to endorse or promote products derived from this
*    software without prior written permission. For written 
*    permission, please contact apache@apache.org.
*
* 5. Products derived from this software may not be called "Apache 
*    XMLBeans", nor may "Apache" appear in their name, without prior 
*    written permission of the Apache Software Foundation.
*
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
* OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
* ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
* SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
* LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
* USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
* OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
* OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
* SUCH DAMAGE.
* ====================================================================
*
* This software consists of voluntary contributions made by many
* individuals on behalf of the Apache Software Foundation and was
* originally based on software copyright (c) 2000-2003 BEA Systems 
* Inc., <http://www.bea.com/>. For more information on the Apache Software
* Foundation, please see <http://www.apache.org/>.
*/

package org.apache.xmlbeans.impl.tool;

import com.bea.x2003.x05.xmlbean.ltgfmt.TestsDocument;
import com.bea.x2003.x05.xmlbean.ltgfmt.FileDesc;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlCalendar;

import java.io.File;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Date;
import java.util.regex.Pattern;
import java.net.URI;

public class XSTCTester
{
    public static void main(String[] args) throws IOException
    {
        long start = System.currentTimeMillis();
        
        CommandLine cl = new CommandLine(args, Collections.EMPTY_SET);
        boolean showpass = (cl.getOpt("showpass") != null);
        File[] allFiles = cl.getFiles();
        Collection ltgFiles = new ArrayList();
        Harness harness = new XMLBeanXSTCHarness();
        
        for (int i = 0; i < allFiles.length; i++)
        {
            if (allFiles[i].getName().indexOf("LTG") >= 0)
                ltgFiles.add(allFiles[i]);
        }
        
        File resultsFile = new File("out.html");
        PrintWriter writer = new PrintWriter(new FileWriter(resultsFile));
        writer.println("<html>");
        writer.println("<style>td {border-bottom: 1px solid black} xmp {white-space: normal; word-wrap: break-word; word-break: break-all} </style>");
        writer.println("<body>");

        writer.println("<script language='JavaScript' type='text/javascript'>");
        writer.println("var w;");
        writer.println("function openWindow(schema, instance) {");
	    writer.println("  if (w == null) {");
        writer.println("    w = window.open('about:blank', 'xstc');");
	    writer.println("  }");
	    writer.println("  if (w.closed) {");
		writer.println("    w = window.open('about:blank', 'xstc');");
	    writer.println("  }");
	    writer.println("  w.document.open();");
	    writer.println("  w.document.write(\"<frameset rows=*,*><frame src='\" + schema + \"'><frame src='\" + instance + \"'></frameset>\");");
	    writer.println("  w.document.close();");
	    writer.println("  w.focus();");
        writer.println("}");
        writer.println("</script>");

        writer.println("<h1>XML Schema Test Collection Results</h1>");
        writer.println("<p>Run on " + (new XmlCalendar(new Date())) + "</p>");
        writer.println("<table style='border: 1px solid black' cellpadding=0 cellspacing=0>");
        writer.println("<tr><td width=75%>Description</td><td width=12.5%>sch v</td><td width=12.5%>ins v</td></tr>");
        int failures = 0;
        int cases = 0;
        for (Iterator i = ltgFiles.iterator(); i.hasNext(); )
        {
            File ltgFile = (File)i.next();
            System.out.println("Processing test cases in " + ltgFile);
            Collection ltgErrors = new ArrayList();
            TestCase[] testCases = parseLTGFile(ltgFile, ltgErrors);
            Collection results = new ArrayList();
            if (testCases != null) for (int j = 0; j < testCases.length; j++)
            {
                TestCaseResult result = new TestCaseResult();
                result.testCase = testCases[j];
                harness.runTestCase(result);
                cases += 1;
                if (!result.succeeded())
                    failures += 1;
                else if (!showpass)
                    continue;
                results.add(result);
            }
            writer.println("<tr><td colspan=3 bgcolor=skyblue>" + ltgFile + "</td></tr>");
            if (!ltgErrors.isEmpty())
            {
                writer.println("<tr><td>Errors within the LTG file:");
                writer.println("<xmp>");
                for (Iterator j = ltgErrors.iterator(); j.hasNext(); )
                    writer.println(j.next());
                writer.println("</xmp>");
                writer.println("</td></tr>");
            }
            else
            {
                if (results.size() == 0)
                    writer.println("<tr><td colspan=3 bgcolor=green>Nothing to report</td></tr>");
            }
            if (results == null)
                continue;
            for (Iterator j = results.iterator(); j.hasNext() ;)
            {
                summarizeResultAsHTMLTableRows((TestCaseResult)j.next(), writer);
            }
        }
        writer.println("<tr><td colspan=3>Summary: " + failures + " failures out of " + cases + " cases run.</td></tr>");
        writer.println("</table>");
        writer.close();
        
        long finish = System.currentTimeMillis();
        System.out.println("Time run tests: " + ((double)(finish - start) / 1000.0) + " seconds" );
        
        // Launch results
        if (System.getProperty("os.name").toLowerCase().indexOf("windows") >= 0)
            Runtime.getRuntime().exec("cmd /c start iexplore \"" + resultsFile.getAbsolutePath() + "\"");
        else
            System.out.println("Results output to " + resultsFile);
    }
    
    public static class TestCase
    {
        private File ltgFile;
        private String id;
        private String origin;
        private String description;
        private File schemaFile;
        private File instanceFile;
        private File resourceFile;
        private boolean svExpected;
        private boolean ivExpected;
        private boolean rvExpected;

        public File getLtgFile()
        {
            return ltgFile;
        }

        public String getId()
        {
            return id;
        }

        public String getOrigin()
        {
            return origin;
        }

        public String getDescription()
        {
            return description;
        }

        public File getSchemaFile()
        {
            return schemaFile;
        }

        public File getInstanceFile()
        {
            return instanceFile;
        }

        public File getResourceFile()
        {
            return resourceFile;
        }

        public boolean isSvExpected()
        {
            return svExpected;
        }

        public boolean isIvExpected()
        {
            return ivExpected;
        }

        public boolean isRvExpected()
        {
            return rvExpected;
        }
    }
    
    public static class TestCaseResult
    {
        private TestCase testCase;
        private boolean svActual;
        private Collection svMessages = new ArrayList();
        private boolean ivActual;
        private Collection ivMessages = new ArrayList();
        private boolean crash;

        public TestCase getTestCase()
        {
            return testCase;
        }

        public boolean isSvActual()
        {
            return svActual;
        }

        public void setSvActual(boolean svActual)
        {
            this.svActual = svActual;
        }

        public boolean isIvActual()
        {
            return ivActual;
        }

        public void setIvActual(boolean ivActual)
        {
            this.ivActual = ivActual;
        }

        public Collection getSvMessages()
        {
            return Collections.unmodifiableCollection(svMessages);
        }

        public void addSvMessages(Collection svMessages)
        {
            this.svMessages.addAll(svMessages);
        }

        public Collection getIvMessages()
        {
            return Collections.unmodifiableCollection(ivMessages);
        }

        public void addIvMessages(Collection ivMessages)
        {
            this.ivMessages.addAll(ivMessages);
        }
        
        public void setCrash(boolean crash)
        {
            this.crash = crash;
        }
        
        public boolean isCrash()
        {
            return crash;
        }
        
        public boolean succeeded()
        {
            return !crash && (isIvActual() == testCase.isIvExpected()) && (isSvActual() == testCase.isSvExpected());
        }
    }
    
    public static interface Harness
    {
        public void runTestCase(TestCaseResult result);
    }
    
    public static String makeHTMLLink(File file, boolean value)
    {
        if (file == null)
            return "&nbsp;";
        URI uri = file.getAbsoluteFile().toURI();
        return "<a href=\"" + uri + "\" target=_blank>" + Boolean.toString(value) + "</a>";
    }
    
    private static final Pattern leadingSpace = Pattern.compile("^\\s+", Pattern.MULTILINE);
    
    public static String makeHTMLDescription(TestCase testCase)
    {
        StringBuffer sb = new StringBuffer();
        sb.append("<a class=noline href='javascript:openWindow(\"");
        if (testCase.getSchemaFile() == null)
            sb.append("about:No schema");
        else
            sb.append(testCase.getSchemaFile().getAbsolutePath().replaceAll("\\\\", "\\\\\\\\"));

        sb.append("\", \"");
        if (testCase.getInstanceFile() == null)
            sb.append("about:No instance");
        else
            sb.append(testCase.getInstanceFile().getAbsolutePath().replaceAll("\\\\", "\\\\\\\\"));
        sb.append("\")'><xmp>");
        sb.append(leadingSpace.matcher(testCase.getDescription()).replaceAll("") + "(" + testCase.getId() + ")");
        sb.append("</xmp></a>");
        return sb.toString();
    }
    
    public static void summarizeResultAsHTMLTableRows(TestCaseResult result, PrintWriter out)
    {
        TestCase testCase = result.getTestCase();
        boolean sRight = testCase.getSchemaFile() == null || testCase.isSvExpected() == result.isSvActual();
        boolean iRight = testCase.getInstanceFile() == null || testCase.isIvExpected() == result.isIvActual();
        
        out.println(result.isCrash() ? "<tr bgcolor=black color=white>" : "<tr>");
        out.println("<td valign=top>" + makeHTMLDescription(testCase) + "</td>");
        String sLinks;
        if (testCase.getResourceFile() == null)
            sLinks = makeHTMLLink(testCase.getSchemaFile(), result.isSvActual());
        else
            sLinks = makeHTMLLink(testCase.getSchemaFile(), result.isSvActual()) + "<br>" + makeHTMLLink(testCase.getResourceFile(), result.isSvActual());
        
        out.println((sRight ? "<td valign=top>" : result.isSvActual() ? "<td bgcolor=orange valign=top>" : "<td bgcolor=red valign=top>") + sLinks + "</td>");
        out.println((iRight ? "<td valign=top>" : result.isIvActual() ? "<td bgcolor=orange valign=top>" : "<td bgcolor=red valign=top>") + makeHTMLLink(testCase.getInstanceFile(), result.isIvActual()) + "</td>");
        out.println("</tr>");
        if (!result.getIvMessages().isEmpty() || !result.getSvMessages().isEmpty())
        {
            if (!result.succeeded())
                out.println("<tr><td colspan=3 bgcolor=yellow><xmp>");
            else
                out.println("<tr><td colspan=3><xmp>");
            for (Iterator j = result.getSvMessages().iterator(); j.hasNext(); )
                out.println(j.next());
            for (Iterator j = result.getIvMessages().iterator(); j.hasNext(); )
                out.println(j.next());
            out.println("</xmp></tr></td>");
        }
    }
    
    public static TestCase[] parseLTGFile(File ltgFile, Collection outerErrors)
    {
        Collection errors = new ArrayList();
        try
        {
            XmlOptions ltgOptions = new XmlOptions();
            ltgOptions.setLoadSubstituteNamespaces(Collections.singletonMap("", "http://www.bea.com/2003/05/xmlbean/ltgfmt"));
            ltgOptions.setErrorListener(errors);
            ltgOptions.setLoadLineNumbers();
            TestsDocument doc = TestsDocument.Factory.parse(ltgFile, ltgOptions);
            if (!doc.validate(ltgOptions))
                throw new Exception("Document " + ltgFile + " not valid.");
            
            com.bea.x2003.x05.xmlbean.ltgfmt.TestCase[] testCases = doc.getTests().getTestArray();
            
            Collection result = new ArrayList();
            for (int i = 0; i < testCases.length; i++)
            {
                TestCase newCase = new TestCase();
                newCase.ltgFile = ltgFile;
                newCase.id = testCases[i].getId();
                newCase.origin = testCases[i].getOrigin();
                newCase.description = testCases[i].getDescription();
                FileDesc[] filedescs = testCases[i].getFiles().getFileArray();
                testCases[i].getOrigin();
                for (int j = 0; j < filedescs.length; j++)
                {
                    String dir = filedescs[j].getFolder();
                    String filename = filedescs[j].getFileName();
                    File theFile = new File(ltgFile.getParentFile(), dir + "/" + filename);
                    if (!theFile.exists() || !theFile.isFile() || !theFile.canRead())
                    {
                        outerErrors.add(XmlError.forObject("Can't read file " + theFile, filedescs[j]).toString());
                        continue;
                    }
                    
                    switch (filedescs[j].getRole().intValue())
                    {
                        case FileDesc.Role.INT_INSTANCE:
                            if (newCase.instanceFile != null)
                                outerErrors.add(XmlError.forObject("More than one instance file speicifed - ignoring all but last", filedescs[j]).toString());
                            newCase.instanceFile = theFile;
                            newCase.ivExpected = filedescs[j].getValidity();
                            break;
                            
                        case FileDesc.Role.INT_SCHEMA:
                            if (newCase.schemaFile != null)
                                outerErrors.add(XmlError.forObject("More than one schema file speicifed - ignoring all but last", filedescs[j]).toString());
                            newCase.schemaFile = theFile;
                            newCase.svExpected = filedescs[j].getValidity();
                            break;
                            
                        case FileDesc.Role.INT_RESOURCE:
                            if (newCase.resourceFile != null)
                                outerErrors.add(XmlError.forObject("More than one resource file speicifed - ignoring all but last", filedescs[j]).toString());
                            newCase.resourceFile = theFile;
                            newCase.rvExpected = filedescs[j].getValidity();
                            break;
                        
                        default:
                            throw new XmlException(XmlError.forObject("Unexpected file role", filedescs[j]));
                    }
                }
                result.add(newCase);
            }
            return (TestCase[])result.toArray(new TestCase[result.size()]);
        }
        catch (Exception e)
        {
            if (errors.isEmpty())
                outerErrors.add(e.getMessage());
            else for (Iterator i = errors.iterator(); i.hasNext(); )
                outerErrors.add(i.next().toString());
            return null;
        }
    }
    
}
