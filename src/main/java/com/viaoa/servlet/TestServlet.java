package com.viaoa.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

public class TestServlet extends HttpServlet {
    private Thread thread;
    
    @Override
    public void init() throws ServletException {
        super.init();
        
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // qqqqqqqqq call your code here
            }
        });
        thread.start();  //qqqqqq run in your own thread
    }
}
