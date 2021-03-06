package org.siouan.frontendgradleplugin.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.MalformedURLException;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link NodeDistributionUrlResolver} class.
 */
class NodeDistributionUrlResolverTest {

    private static final String VERSION = "3.5.2";

    @Test
    void shouldFailWhenBuildingInstanceWithNullVersionAndNullDistributionUrl() {
        assertThatThrownBy(() -> new NodeDistributionUrlResolver(null, null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldFailWhenResolvingWithInvalidDistributionUrl() {
        assertThatThrownBy(() -> new NodeDistributionUrlResolver(null, "fot://greg:://grrg:").resolve())
            .isInstanceOf(DistributionUrlResolverException.class).hasCauseInstanceOf(MalformedURLException.class);
    }

    @Test
    void shouldReturnDefaultUrlWhenResolvingWithVersionAndNoDistributionUrl() throws DistributionUrlResolverException {
        assertThat(new NodeDistributionUrlResolver(VERSION, null).resolve()).isNotNull();
    }

    @Test
    void shouldReturnDistributionUrlWhenResolvingWithNoVersionAndDistributionUrl()
        throws DistributionUrlResolverException {
        final String distributionUrl = "http://url";
        assertThat(new NodeDistributionUrlResolver(null, distributionUrl).resolve().toString())
            .isEqualTo(distributionUrl);
    }

    @Test
    void shouldResolveUrlWhenOsIsWindowsNTAndJreArchIsX86() throws DistributionUrlResolverException {
        assertThat(new NodeDistributionUrlResolver(VERSION, null, "Windows NT", "x86").resolve().toString())
            .endsWith("-win-x86.zip");
    }

    @Test
    void shouldResolveUrlWhenOsIsWindowsNTAndJreArchIsX64() throws DistributionUrlResolverException {
        assertThat(new NodeDistributionUrlResolver(VERSION, null, "Windows NT", "x64").resolve().toString())
            .endsWith("-win-x64.zip");
    }

    @Test
    void shouldResolveUrlWhenOsIsLinuxAndJreArchIsAmd64() throws DistributionUrlResolverException {
        assertThat(new NodeDistributionUrlResolver(VERSION, null, "Linux", "amd64").resolve().toString())
            .endsWith("-linux-x64.tar.gz");
    }

    @Test
    void shouldFailWhenOsIsLinuxAndJreArchIsI386() {
        assertThatThrownBy(() -> new NodeDistributionUrlResolver(VERSION, null, "Linux", "i386").resolve())
            .isInstanceOf(DistributionUrlResolverException.class).hasNoCause();
    }

    @Test
    void shouldResolveUrlWhenOsIsMacAndJreArchIsPPC() throws DistributionUrlResolverException {
        assertThat(new NodeDistributionUrlResolver(VERSION, null, "Mac OS X", "ppc").resolve().toString())
            .endsWith("-darwin-x64.tar.gz");
    }

    @Test
    void shouldFailWhenOsIsSolarisAndJreArchIsSparc() {
        assertThatThrownBy(() -> new NodeDistributionUrlResolver(VERSION, null, "Solaris", "sparc").resolve())
            .isInstanceOf(DistributionUrlResolverException.class).hasNoCause();
    }
}
