/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.kpax.winfoom.pac.net;


import inet.ipaddr.IPAddressString;
import org.kpax.winfoom.exception.CheckedExceptionWrapper;
import org.kpax.winfoom.exception.NativeException;
import org.kpax.winfoom.util.functional.SingletonSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * IP address utilities: resolving hostname, comparing IP addresses.
 */
public class IpAddressUtils {

    public static final String LOCALHOST = "127.0.0.1";

    /**
     * A supplier for all primary IP addresses of the current Windows machine. The {@link SingletonSupplier#get()}
     * may throw a {@link CheckedExceptionWrapper} exception.
     */
    public static final SingletonSupplier<InetAddress[]> allPrimaryAddresses = new SingletonSupplier<>(() -> {
        try {
            return InetAddress.getAllByName(HostnameUtils.removeDomain(HostnameUtils.getHostName()));
        } catch (UnknownHostException e) {
            throw new CheckedExceptionWrapper(e);
        } catch (NativeException e) {
            throw new CheckedExceptionWrapper(new UnknownHostException(e.getMessage() + ", error code : " + e.getErrorCode()));
        }
    });

    /**
     * A supplier for the primary IPv4 address of the current Windows machine.The {@link SingletonSupplier#get()}
     * may throw a {@link CheckedExceptionWrapper} exception.
     */
    public static final SingletonSupplier<InetAddress> primaryIPv4Address = new SingletonSupplier<>(() -> {
        try {
            return Arrays.stream(allPrimaryAddresses.get()).
                    filter(a -> a.getClass() == Inet4Address.class).
                    findFirst().orElseThrow(() -> new UnknownHostException("No IPv4 address found"));
        } catch (UnknownHostException e) {
            throw new CheckedExceptionWrapper(e);
        }
    });

    /**
     * A {@link Comparator} that favors the {@link Inet6Address} addresses over the  {@link Inet4Address}.
     * Addresses of same type are not ordered.
     */
    public static final Comparator<InetAddress> IPv6_FIRST_COMPARATOR = (a1, a2) -> {
        if (a1.getClass() == Inet4Address.class && a2.getClass() == Inet6Address.class) {
            return 1;
        } else if (a1.getClass() == Inet6Address.class && a2.getClass() == Inet4Address.class) {
            return -1;
        } else {
            return 0;
        }
    };

    /**
     * A {@link Comparator} that favors the {@link Inet4Address} addresses over the  {@link Inet6Address}.
     * Addresses of same type are not ordered.
     */
    public static final Comparator<InetAddress> IPv4_FIRST_COMPARATOR = (a1, a2) -> {
        if (a1.getClass() == Inet4Address.class && a2.getClass() == Inet6Address.class) {
            return -1;
        } else if (a1.getClass() == Inet6Address.class && a2.getClass() == Inet4Address.class) {
            return 1;
        } else {
            return 0;
        }
    };

    /**
     * A {@link Comparator} that favors the {@link Inet6Address} addresses over the  {@link Inet4Address}.
     * Addresses of same type are ordered by comparing byte to byte {@link #compareByteByByte(InetAddress, InetAddress)}.
     */
    public static final Comparator<InetAddress> IPv6_FIRST_TOTAL_ORDERING_COMPARATOR = (a1, a2) -> {
        int compareByType = IPv6_FIRST_COMPARATOR.compare(a1, a2);
        if (compareByType == 0) {
            return compareByteByByte(a1, a2);
        }
        return compareByType;
    };

    private static final Logger logger = LoggerFactory.getLogger(IpAddressUtils.class);

    IpAddressUtils() {
    }

    /**
     * A  byte-by-byte {@link InetAddress} comparator.
     *
     * @param a1 first address
     * @param a2 second address
     * @return {@code 1} if the first address is bigger,  {@code -1} if the second address is bigger or {@code 0} otherwise.
     */
    public static int compareByteByByte(InetAddress a1, InetAddress a2) {
        byte[] bArr1 = a1.getAddress();
        byte[] bArr2 = a2.getAddress();
        // Compare byte-by-byte.
        for (int i = 0; i < bArr1.length; i++) {
            int x1 = Byte.toUnsignedInt(bArr1[i]);
            int x2 = Byte.toUnsignedInt(bArr2[i]);

            if (x1 == x2) {
                continue;
            }
            if (x1 < x2) {
                return -1;
            } else {
                return 1;
            }
        }
        return 0;
    }

    /**
     * A selective getter for address comparators.
     *
     * @param preferIPv6Addresses whether to prefer the IPv6 addresses.
     * @return {@link #IPv6_FIRST_COMPARATOR} if prefer the IPv6 addresses, {@link #IPv4_FIRST_COMPARATOR} otherwise
     */
    public static Comparator<InetAddress> addressComparator(boolean preferIPv6Addresses) {
        return preferIPv6Addresses ? IPv6_FIRST_COMPARATOR : IPv4_FIRST_COMPARATOR;
    }

    /**
     * @see #resolve(String, Predicate)
     */
    public static List<InetAddress> resolve(String host)
            throws UnknownHostException {
        return resolve(host, null);
    }

    /**
     * If the host is an IP address (IPv4 or IPv6), then the corresponding {@link InetAddress} is returned.
     * Otherwise the host is DNS resolved.
     *
     * @param host   the IP address or hostname
     * @param filter for filtering the result
     * @return the filtered list (possible empty) of {@link InetAddress} instances
     * @throws UnknownHostException if no IP address for the host could be found, or if a scope_id was specified for a global IPv6 address
     */
    public static List<InetAddress> resolve(String host,
                                            Predicate<InetAddress> filter)
            throws UnknownHostException {
        if (isValidIPAddress(host)) {
            // No DNS lookup is needed in this case
            InetAddress address = InetAddress.getByName(host);
            if (filter == null || filter.test(address)) {
                return Collections.singletonList(address);
            } else {
                return Collections.emptyList();
            }
        } else {
            InetAddress[] ipAddresses = InetAddress.getAllByName(host);
            Stream<InetAddress> addressStream = Arrays.stream(ipAddresses);
            if (filter != null) {
                addressStream = addressStream.filter(filter);
            }
            return addressStream.collect(Collectors.toList());
        }
    }

    public static boolean isValidIPAddress(String address) {
        try {
            IPAddressString addrString = new IPAddressString(address);
            addrString.toAddress();
            return true;
        } catch (Exception e) {
            logger.debug("Not a valid IP address", e);
            return false;
        }
    }

    public static boolean isValidIPv4Address(String address) {
        try {
            IPAddressString addrString = new IPAddressString(address);
            return addrString.toAddress().isIPv4();
        } catch (Exception e) {
            logger.debug("Not a valid IP address", e);
            return false;
        }
    }

    public static boolean isValidIPv6Address(String address) {
        try {
            IPAddressString addrString = new IPAddressString(address);
            return addrString.toAddress().isIPv6();
        } catch (Exception e) {
            logger.debug("Not a valid IP address", e);
            return false;
        }
    }

}