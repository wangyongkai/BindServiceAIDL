// DataService.aidl.aidl
package com.testservice.bindservice;
// Declare any non-default types here with import statements
import com.testservice.bindservice.ServiceDataListener;//必须导入
import com.testservice.bindservice.Book;
interface DataService{
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
   Book getConuntAIDL(int i);
   void addBook(in Book book);
   void setListener(ServiceDataListener listener);
   String stopService();

}
