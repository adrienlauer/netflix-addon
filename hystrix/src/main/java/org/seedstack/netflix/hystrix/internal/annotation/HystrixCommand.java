/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.netflix.hystrix.internal.annotation;

import java.lang.annotation.*;

/**
 * This annotation is used to specify some methods which should be processed as hystrix commands.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HystrixCommand {

    /**
     * Specifies a method to process fallback logic.
     * A fallback method should be defined in the same class where is HystrixCommand.
     * Also a fallback method should have the same signature to a method which was invoked as hystrix command.
     * For example:
     * <p>
     * <code>
     * {@literal @}HystrixCommand(fallbackMethod = "getByIdFallback")<br>
     * public String getById(String id) {...}<br>
     * <br>
     * private String getByIdFallback(String id) {...}
     * </code>
     * </p>
     * Also a fallback method can be annotated with {@link HystrixCommand}
     *
     * @return method name
     */
    String fallbackMethod() default "";

    /**
     * The command group key is used for grouping together commands such as for reporting,
     * alerting, dashboards or team/library ownership.
     * <p/>
     * default => the runtime class name of annotated method (<code>Method#getName();</code>).
     *
     * @return group key
     * @see com.netflix.hystrix.HystrixCommandGroupKey
     */
    String groupKey() default "";

    /**
     * The Hystrix command key is used to identify a command instance for statistics, circuit-breaker, properties, etc.
     * <p/>
     * default => the runtime class name of annotated method (<code>Method#getName();</code>).
     *
     * @return command key
     * @see com.netflix.hystrix.HystrixCommandKey
     */
    String commandKey() default "";



}
