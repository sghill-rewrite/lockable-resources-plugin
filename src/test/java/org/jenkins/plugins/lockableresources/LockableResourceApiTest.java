/* SPDX-License-Identifier: MIT
 * Copyright (c) 2020, Tobias Gruetzmacher
 */
package org.jenkins.plugins.lockableresources;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;

import hudson.security.FullControlOnceLoggedInAuthorizationStrategy;
import org.htmlunit.FailingHttpStatusCodeException;
import org.jenkins.plugins.lockableresources.util.Constants;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;

public class LockableResourceApiTest {

    // ---------------------------------------------------------------------------
    @Before
    public void setUp() {
        // to speed up the test
        System.setProperty(Constants.SYSTEM_PROPERTY_DISABLE_SAVE, "true");
    }

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void reserveUnreserveApi() throws Exception {
        LockableResourcesManager.get().createResource("a1");

        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        j.jenkins.setAuthorizationStrategy(new FullControlOnceLoggedInAuthorizationStrategy());

        JenkinsRule.WebClient wc = j.createWebClient();
        wc.login("user");
        TestHelpers testHelpers = new TestHelpers();
        testHelpers.clickButton("reserve", "a1");
        assertThat(LockableResourcesManager.get().fromName("a1").isReserved(), is(true));
        testHelpers.clickButton("unreserve", "a1");
        assertThat(LockableResourcesManager.get().fromName("a1").isReserved(), is(false));
    }

    @Test
    @Issue("SECURITY-1958")
    public void apiUsageHttpGet() {
        JenkinsRule.WebClient wc = j.createWebClient();
        FailingHttpStatusCodeException e = assertThrows(
                FailingHttpStatusCodeException.class, () -> wc.goTo("lockable-resources/reserve?resource=resource1"));
        assertThat(e.getStatusCode(), is(405));
    }
}
