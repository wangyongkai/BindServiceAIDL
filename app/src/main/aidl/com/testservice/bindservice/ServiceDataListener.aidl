// ServiceDataListener.aidl
package com.testservice.bindservice;
// Declare any non-default types here with import statements
import com.testservice.bindservice.Book;
interface ServiceDataListener {
void getCountFromService(in Book book);
}
