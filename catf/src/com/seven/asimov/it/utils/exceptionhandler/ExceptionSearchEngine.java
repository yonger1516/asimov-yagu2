package com.seven.asimov.it.utils.exceptionhandler;

import android.content.Context;
import android.content.res.AssetManager;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ExceptionSearchEngine {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionSearchEngine.class.getSimpleName());

    private static List<ExceptionSearchRule> statedRules;

    public static void init(Context context) {
        statedRules = new ArrayList<ExceptionSearchRule>();
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();

            DefaultHandler handler = new DefaultHandler() {
                String sourceClass = "";
                String sourceMethod = "";
                String throwableClass = "";
                String logMessage = "";

                boolean b_sourceClass = false;
                boolean b_sourceMethod = false;
                boolean b_throwableClass = false;
                boolean b_logMessage = false;
                boolean b_blockStart = false;

                @Override
                public void startElement(String uri, String localName, String qName,
                                         Attributes attributes) throws SAXException {

                    logger.trace("Start Element :" + qName);

                    if (qName.equalsIgnoreCase("exception")) {
                        b_blockStart = true;
                        logger.trace("Found Start Element :" + qName);
                    }
                    if (b_blockStart && qName.equalsIgnoreCase("sourceClass")) {
                        b_sourceClass = true;
                    }
                    if (b_blockStart && qName.equalsIgnoreCase("sourceMethod")) {
                        b_sourceMethod = true;
                    }
                    if (b_blockStart && qName.equalsIgnoreCase("throwableClass")) {
                        b_throwableClass = true;
                    }
                    if (b_blockStart && qName.equalsIgnoreCase("logMessage")) {
                        b_logMessage = true;
                    }
                }

                @Override
                public void endElement(String uri, String localName,
                                       String qName) throws SAXException {
                    logger.trace("End Element :" + qName);
                    if (qName.equalsIgnoreCase("exception")) {
                        try {
                            logger.trace(new StringBuilder("endElement   ").append(" sourceClass:").append(sourceClass).
                                    append(" sourceMethod:").append(sourceMethod).append(" throwableClass:").append(throwableClass).
                                    append(" logMessage:").append(logMessage).
                                    toString());
                            ExceptionSearchRule rule = new ExceptionSearchRule(sourceClass, sourceMethod, throwableClass, logMessage);
                            logger.trace(rule.toString());
                            statedRules.add(rule);
                        } catch (NoSuchMethodException nme) {
                            logger.warn(ExceptionUtils.getStackTrace(nme));
                        } catch (ClassNotFoundException cnfe) {
                            logger.warn(ExceptionUtils.getStackTrace(cnfe));
                        } catch (Exception e) {
                            logger.error(ExceptionUtils.getStackTrace(e));
                        }

                        b_blockStart = false;
                        sourceClass = sourceMethod = throwableClass = logMessage = "";
                        logger.trace("Found End Element :" + qName);
                    }

                }

                @Override
                public void characters(char ch[], int start, int length) throws SAXException {
                    if (b_sourceClass) {
                        sourceClass = new String(ch, start, length);
                        b_sourceClass = false;
                    }
                    if (b_sourceMethod) {
                        sourceMethod = new String(ch, start, length);
                        b_sourceMethod = false;
                    }
                    if (b_throwableClass) {
                        throwableClass = new String(ch, start, length);
                        b_throwableClass = false;
                    }
                    if (b_logMessage) {
                        logMessage = new String(ch, start, length);
                        b_logMessage = false;
                    }
                }

            };
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open("exception_rules.xml");
            saxParser.parse(inputStream, handler);
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }

    public static void searchRule(ExceptionCharacteristic ec) {
        logger.trace(ec.toString());
        List<ExceptionSearchRule> foundRules = new ArrayList<ExceptionSearchRule>();
        foundRules.addAll(ec.getRules());
        for (ExceptionSearchRule rule : statedRules) {
            if (ec.getRules().contains(rule)) {
                logger.error("rule message=" + rule.getLogMessage());
            }
        }
    }
}
