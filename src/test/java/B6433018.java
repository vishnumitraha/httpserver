/*
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/**
 * @test
 * @bug 6433018
 * @summary  HTTP server sometimes sends bad request for browsers javascript
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.jboss.com.sun.net.httpserver.Headers;
import org.jboss.com.sun.net.httpserver.HttpContext;
import org.jboss.com.sun.net.httpserver.HttpExchange;
import org.jboss.com.sun.net.httpserver.HttpHandler;
import org.jboss.com.sun.net.httpserver.HttpServer;

public class B6433018 {

    static String CRLF = "\r\n";

    /* invalid HTTP POST with extra CRLF at end */
    /* This checks that the server is able to handle it
     * and recognise the second request */

    static String cmd =
        "POST /test/item HTTP/1.1"+CRLF+
        "Keep-Alive: 300"+CRLF+
        "Proxy-Connection: keep-alive"+CRLF+
        "Content-Type: text/xml"+CRLF+
        "Content-Length: 22"+CRLF+
        "Pragma: no-cache"+CRLF+
        "Cache-Control: no-cache"+CRLF+ CRLF+
        "<item desc=\"excuse\" />"+CRLF+
        "GET /test/items HTTP/1.1"+CRLF+
        "Host: araku:9999"+CRLF+
        "Accept-Language: en-us,en;q=0.5"+CRLF+
        "Accept-Encoding: gzip,deflate"+CRLF+
        "Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.7"+CRLF+
        "Keep-Alive: 300"+CRLF+
        "Proxy-Connection: keep-alive"+CRLF+
        "Pragma: no-cache"+CRLF+
        "Cache-Control: no-cache"+CRLF+CRLF;

    public static void main (String[] args) throws Exception {
        Handler handler = new Handler();
        InetSocketAddress addr = new InetSocketAddress (0);
        HttpServer server = HttpServer.create (addr, 0);
        HttpContext ctx = server.createContext ("/test", handler);

        server.start ();

        Socket s = new Socket ("localhost", server.getAddress().getPort());

        try {
            OutputStream os = s.getOutputStream();
            os.write (cmd.getBytes());
            Thread.sleep (3000);
            s.close();
        } catch (IOException e) { }
        server.stop(2);
        if (requests != 2) {
            throw new RuntimeException ("did not receive the 2 requests");
        }
        System.out.println ("OK");
    }

    public static boolean error = false;
    static int requests = 0;

    static class Handler implements HttpHandler {
        int invocation = 1;
        public void handle (HttpExchange t)
            throws IOException
        {
            InputStream is = t.getRequestBody();
            Headers map = t.getRequestHeaders();
            Headers rmap = t.getResponseHeaders();
            while (is.read () != -1) ;
            is.close();
            t.sendResponseHeaders (200, -1);
            t.close();
            requests ++;
        }
    }
}
