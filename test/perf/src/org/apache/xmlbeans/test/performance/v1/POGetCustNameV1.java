/*   Copyright 2004 The Apache Software Foundation
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*  limitations under the License.
*/
package org.apache.xmlbeans.test.performance.v1;

import java.io.CharArrayReader;

import org.apache.xmlbeans.test.performance.utils.PerfUtil;
import org.apache.xmlbeans.test.performance.utils.Constants;

// from v2-generated schema jar(s)
import org.openuri.easypo.PurchaseOrderDocument;
import org.openuri.easypo.PurchaseOrderDocument.PurchaseOrder;
import org.openuri.easypo.Customer;


public class POGetCustNameV1
{
  public static void main(String[] args) throws Exception
  {

    final int iterations = Constants.GET_SET_ITERATIONS;
    String filename;

    // the xml instance can be specified by either a number
    // or the name of a file located in the test folder
    // see Constants.java
    if(args.length == 0){
      filename = Constants.PO_INSTANCE_1;
    }
    else if(args[0].length() > 1){
      filename = Constants.XSD_DIR+Constants.P+args[0];
    }
    else{
      switch( Integer.parseInt(args[0]) )
      {
      case 1: filename = Constants.PO_INSTANCE_1; break;
      case 2: filename = Constants.PO_INSTANCE_2; break;  
      case 3: filename = Constants.PO_INSTANCE_3; break;
      case 4: filename = Constants.PO_INSTANCE_4; break;
      case 5: filename = Constants.PO_INSTANCE_5; break;
      case 6: filename = Constants.PO_INSTANCE_6; break;
      case 7: filename = Constants.PO_INSTANCE_7; break;
      default: filename = Constants.PO_INSTANCE_1; break;
      }
    }    

    POGetCustNameV1 test = new POGetCustNameV1();
    PerfUtil util = new PerfUtil();
    long cputime;
    int hash = 0;


    // get the xmlinstance
    char[] chars = util.fileToChars(filename);
        
    // parse the instance
    PurchaseOrderDocument podoc = 
      PurchaseOrderDocument.Factory.parse(new CharArrayReader(chars));
    PurchaseOrder po = podoc.getPurchaseOrder();
    Customer customer = po.getCustomer();

    // warm up the vm
    cputime = System.currentTimeMillis();
    for(int i=0; i<iterations; i++){
      hash += test.run(customer);
    }
    cputime = System.currentTimeMillis() - cputime;

    // run it again for the real measurement
    cputime = System.currentTimeMillis();
    for(int i=0; i<iterations; i++){     
      hash += test.run(customer);
    }
    cputime = System.currentTimeMillis() - cputime;
      
    // print the results
    System.out.print(Constants.DELIM+POGetCustNameV1.class.getName()+" filesize="+chars.length+" ");
    System.out.print("hash "+hash+" ");
    System.out.print("time "+cputime+"\n");
  }

  private int run(Customer p_customer) throws Exception
  {
    return p_customer.getName().length() * 17;
  }

}