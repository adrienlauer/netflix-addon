/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.netflix.feign.fixtures;

import feign.Headers;
import feign.RequestLine;

@Headers("Accept: application/json")
public interface TestAPI {

    @RequestLine("GET /message")
    Message getMessage();

    @RequestLine("GET /404")
    Message get404();

}
