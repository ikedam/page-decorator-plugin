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

import java.io.IOException;

import javax.servlet.ServletException;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.RequirePOST;

import jenkins.model.Jenkins;
import hudson.Extension;
import hudson.model.ManagementLink;
import hudson.model.PageDecorator;
import hudson.util.FormApply;

/**
 * {@link PageDecorator} configurable in "Manage Jenkins".
 */
@Extension
public class ConfigurablePageDecorator extends PageDecorator {
    private String header;
    private String footer;
    
    /**
     * ctor
     */
    public ConfigurablePageDecorator() {
        load();
    }
    
    /**
     * @param header
     */
    public void setHeader(String header) {
        this.header = header;
    }
    
    /**
     * @return
     */
    public String getHeader() {
        return header;
    }
    
    /**
     * @param footer
     */
    public void setFooter(String footer) {
        this.footer = footer;
    }
    
    /**
     * @return
     */
    public String getFooter() {
        return footer;
    }
    
    /**
     * Displays the link to the configuration page of {@link ConfigurablePageDecorator}
     * in "Manage Jenkins".
     */
    @Extension
    public static class ManagementLinkImpl extends ManagementLink {
        /**
         * @return
         * @see hudson.model.Action#getDisplayName()
         */
        @Override
        public String getDisplayName() {
            return Messages.ConfigurablePageDecorator_ManagementLinkImpl_DisplayName();
        }
        
        /**
         * @return
         * @see hudson.model.ManagementLink#getIconFileName()
         */
        @Override
        public String getIconFileName() {
            return "document.gif";
        }
            
        /**
         * @return
         * @see hudson.model.ManagementLink#getUrlName()
         */
        @Override
        public String getUrlName() {
            return "pagedecorator";
        }
        
        /**
         * @return the instance of {@link ConfigurablePageDecorator}
         */
        public ConfigurablePageDecorator getPageDecorator() {
            return Jenkins.getInstance().getDescriptorByType(ConfigurablePageDecorator.class);
        }
        
        /**
         * @see ConfigurablePageDecorator#getHeader()
         */
        public String getHeader() {
            return getPageDecorator().getHeader();
        }
        
        /**
         * @see ConfigurablePageDecorator#getFooter()
         */
        public String getFooter() {
            return getPageDecorator().getFooter();
        }
        
        /**
         * @param req
         * @param rsp
         * @throws IOException
         * @throws ServletException
         */
        @RequirePOST
        public synchronized void doConfigSubmit(StaplerRequest req, StaplerResponse rsp)
                throws IOException, ServletException
        {
            Jenkins.getInstance().checkPermission(Jenkins.ADMINISTER);
            
            ConfigurablePageDecorator pageDecorator = getPageDecorator();
            JSONObject json = req.getSubmittedForm();
            pageDecorator.setHeader(json.getString("header"));
            pageDecorator.setFooter(json.getString("footer"));
            pageDecorator.save();
            FormApply.success("/manage").generateResponse(req, rsp, null);
        }
    }
}
