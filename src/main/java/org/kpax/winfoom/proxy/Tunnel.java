/*
 * Copyright (c) 2020. Eugen Covaci
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package org.kpax.winfoom.proxy;

import org.apache.commons.lang3.Validate;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.kpax.winfoom.util.InputOutputs;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Encapsulates the response of a {@link TunnelConnection}.
 *
 * @author Eugen Covaci {@literal eugen.covaci.q@gmail.com}
 * Created on 3/31/2020
 */
public final class Tunnel implements Closeable {

    private final ManagedHttpClientConnection connection;
    private final HttpResponse response;

    Tunnel(ManagedHttpClientConnection connection, HttpResponse response) {
        Validate.notNull(connection, "connection cannot be null");
        Validate.notNull(response, "response cannot be null");
        this.connection = connection;
        this.response = response;
    }

    ManagedHttpClientConnection getConnection() {
        return connection;
    }

    public InputStream getInputStream() throws IOException {
        return connection.getSocket().getInputStream();
    }

    public OutputStream getOutputStream() throws IOException {
        return connection.getSocket().getOutputStream();
    }

    public HttpResponse getResponse() {
        return response;
    }

    public StatusLine getStatusLine() {
        return response.getStatusLine();
    }

    @Override
    public void close() {
        InputOutputs.close(connection);
    }
}
