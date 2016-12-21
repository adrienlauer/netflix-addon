/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.netflix.feign.internal;

import feign.Feign;
import feign.Logger;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.hystrix.HystrixFeign;
import org.seedstack.netflix.feign.FeignConfig;
import org.seedstack.seed.Configuration;
import org.seedstack.seed.SeedException;
import org.seedstack.shed.reflect.Classes;

import javax.inject.Provider;
import java.lang.reflect.Method;
import java.util.Optional;

class FeignProvider implements Provider<Object> {
    private static final Optional<Class<Object>> HYSTRIX_OPTIONAL = Classes.optional("com.netflix.hystrix.Hystrix");
    @Configuration
    private FeignConfig config;
    private Class<?> feignApi;

    FeignProvider(Class<?> feignApi) {
        this.feignApi = feignApi;
    }

    @Override
    public Object get() {
        FeignConfig.EndpointConfig endpointConfig = config.getEndpoints().get(feignApi);
        Feign.Builder builder = createBuilder(endpointConfig);
        builder.encoder(instantiateEncoder(endpointConfig.getEncoder()));
        builder.decoder(instantiateDecoder(endpointConfig.getDecoder()));
        builder.logger(instantiateLogger(endpointConfig.getLogger()));
        builder.logLevel(endpointConfig.getLogLevel());

        if (endpointConfig.getFallback() != null && builder instanceof HystrixFeign.Builder) {
            return buildHystrixClient(endpointConfig, builder, instantiateFallback(endpointConfig.getFallback()));
        } else {
            return builder.target(feignApi, endpointConfig.getBaseUrl().toExternalForm());
        }
    }

    private Feign.Builder createBuilder(FeignConfig.EndpointConfig endpointConfig) {
        switch (endpointConfig.getHystrixWrapper()) {
            case AUTO:
                return HYSTRIX_OPTIONAL.map(dummy -> (Feign.Builder) HystrixFeign.builder()).orElse(Feign.builder());
            case ENABLED:
                return HYSTRIX_OPTIONAL.map(dummy -> (Feign.Builder) HystrixFeign.builder()).orElseThrow(() -> SeedException.createNew(FeignErrorCode.HYSTRIX_NOT_PRESENT));
            case DISABLED:
                return Feign.builder();
            default:
                throw new IllegalArgumentException("Unsupported Hystrix mode " + endpointConfig.getHystrixWrapper());
        }
    }

    private Object buildHystrixClient(FeignConfig.EndpointConfig endpointConfig, Feign.Builder builder, Object fallback) {
        try {
            Method target = HystrixFeign.Builder.class.getMethod("target", Class.class, String.class, Object.class);
            return target.invoke(builder, feignApi, endpointConfig.getBaseUrl().toExternalForm(), fallback);
        } catch (Exception e) {
            throw SeedException.wrap(e, FeignErrorCode.ERROR_BUILDING_HYSTRIX_CLIENT)
                    .put("class", fallback);
        }
    }

    private Object instantiateFallback(Class<?> fallback) {
        try {
            return fallback.newInstance();
        } catch (Exception e) {
            throw SeedException.wrap(e, FeignErrorCode.ERROR_INSTANTIATING_FALLBACK)
                    .put("class", fallback);
        }
    }

    private Encoder instantiateEncoder(Class<? extends Encoder> encoderClass) {
        try {
            return encoderClass.newInstance();
        } catch (Exception e) {
            throw SeedException.wrap(e, FeignErrorCode.ERROR_INSTANTIATING_ENCODER)
                    .put("class", encoderClass);
        }
    }

    private Decoder instantiateDecoder(Class<? extends Decoder> decoderClass) {
        try {
            return decoderClass.newInstance();
        } catch (Exception e) {
            throw SeedException.wrap(e, FeignErrorCode.ERROR_INSTANTIATING_DECODER)
                    .put("class", decoderClass);
        }
    }

    private Logger instantiateLogger(Class<? extends Logger> loggerClass) {
        try {
            return loggerClass.newInstance();
        } catch (Exception e) {
            throw SeedException.wrap(e, FeignErrorCode.ERROR_INSTANTIATING_LOGGER)
                    .put("class", loggerClass);
        }
    }
}
