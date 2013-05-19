package com.viaoa.util;

public class Test implements TestInterface {


    public Test(){
        
    }
    
    void p(String msg) {
        System.out.println("done");
    }
    
    public void test()  {
        p("test here");
    }
    
    
    public static void main(String[] args) throws Exception {
        Test t = new Test();
        t.test();
        System.out.println("done");
    }
    
}
