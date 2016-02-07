/*
 * The MIT License
 * 
 * Copyright (c) 2015 IKEDA Yasuyuki
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package jp.ikedam.jenkins.plugins.pagedecorator;

import static org.junit.Assert.*;
import jenkins.model.Jenkins;
import hudson.model.FreeStyleProject;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.StringParameterDefinition;
import hudson.security.GlobalMatrixAuthorizationStrategy;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule.WebClient;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import jp.ikedam.jenkins.plugins.pagedecorator.testutils.PageDecoratorJenkinsRule;

/**
 *
 */
public class ConfigurablePageDecoratorTest {
    @ClassRule
    public static PageDecoratorJenkinsRule j = new PageDecoratorJenkinsRule();
    
    @Before
    public void resetJenkins() {
        j.jenkins.setSecurityRealm(null);
        j.jenkins.setAuthorizationStrategy(null);
    }
    
    private ConfigurablePageDecorator getPageDecorator() {
        return j.jenkins.getDescriptorByType(ConfigurablePageDecorator.class);
    }
    
    private HtmlPage getPage() throws Exception {
        FreeStyleProject p = j.createFreeStyleProject();
        p.addProperty(new ParametersDefinitionProperty(
                new StringParameterDefinition("TEST", "")
        ));
        WebClient wc = j.createAllow405WebClient();
        return wc.getPage(p, "build");
    }
    
    @Test
    public void testHeader() throws Exception {
        getPageDecorator().setHeader("<markerForTest>test</markerForTest>");
        getPageDecorator().setFooter("");
        
        HtmlPage page = getPage();
        HtmlElement header = page.getElementsByTagName("head").get(0);
        assertNotNull(header.getElementsByTagName("markerForTest"));
    }
    
    @Test
    public void testFooter() throws Exception {
        getPageDecorator().setHeader("");
        getPageDecorator().setFooter("<markerForTest>test</markerForTest>");
        
        HtmlPage page = getPage();
        HtmlElement body = page.getElementsByTagName("body").get(0);
        assertNotNull(body.getElementsByTagName("markerForTest"));
    }
    
    @Test
    public void testConfiguration() throws Exception {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        GlobalMatrixAuthorizationStrategy auth = new GlobalMatrixAuthorizationStrategy();
        auth.add(Jenkins.ADMINISTER, "admin");
        j.jenkins.setAuthorizationStrategy(auth);
        
        getPageDecorator().setHeader("header");
        getPageDecorator().setFooter("footer");
        
        WebClient wc = j.createWebClient();
        wc.login("admin");
        
        HtmlPage page = wc.goTo(ConfigurablePageDecorator.ManagementLinkImpl.URL);
        j.submit(page.getFormByName("config"));
        
        assertEquals("header", getPageDecorator().getHeader());
        assertEquals("footer", getPageDecorator().getFooter());
    }
    
    @Test
    public void testPermission() throws Exception {
        j.jenkins.setSecurityRealm(j.createDummySecurityRealm());
        GlobalMatrixAuthorizationStrategy auth = new GlobalMatrixAuthorizationStrategy();
        auth.add(Jenkins.ADMINISTER, "admin");
        auth.add(Jenkins.READ, "user");
        j.jenkins.setAuthorizationStrategy(auth);
        
        WebClient wc = j.createWebClient();
        wc.login("user");
        
        wc.setPrintContentOnFailingStatusCode(false);
        try {
            wc.goTo(ConfigurablePageDecorator.ManagementLinkImpl.URL);
            fail();
        } catch (FailingHttpStatusCodeException e) {
        }
    }
}
