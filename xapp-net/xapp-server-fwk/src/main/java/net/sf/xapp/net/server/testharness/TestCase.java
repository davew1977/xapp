package net.sf.xapp.net.server.testharness;

import ngpoker.common.framework.InMessage;

import java.util.List;

public interface TestCase
{
    List<InMessage> getMessages();
}
