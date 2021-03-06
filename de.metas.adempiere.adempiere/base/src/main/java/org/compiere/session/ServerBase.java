package org.compiere.session;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;

import com.google.common.base.MoreObjects;

import de.metas.email.EMail;
import de.metas.email.EMailSentStatus;
import de.metas.logging.LogManager;
import de.metas.process.ProcessExecutionResult;
import de.metas.process.ProcessInfo;
import de.metas.session.jaxrs.IServerService;

/**
 * Base implementation of {@link Server}.
 *
 * AIM: have a technology (EJB or any other) implementaton of {@link Server}
 *
 * @author tsa
 *
 */
public class ServerBase implements IServerService
{
	protected static final transient Logger log = LogManager.getLogger(ServerBase.class);

	private static final AtomicInteger nextInstanceNo = new AtomicInteger(1);
	private final int instanceNo;
	private final AtomicInteger processExecutionCount = new AtomicInteger(0);

	public ServerBase()
	{
		instanceNo = nextInstanceNo.getAndIncrement();
	}

	@Override
	public String toString()
	{
		return MoreObjects.toStringHelper(this)
				.add("instanceNo", instanceNo)
				.add("processExecutionCount", processExecutionCount)
				.toString();
	}

	@Override
	public ProcessExecutionResult process(final int adPInstanceId)
	{
		processExecutionCount.incrementAndGet();

		final Thread currentThread = Thread.currentThread();
		final String threadNameBkp = currentThread.getName();
		try
		{
			currentThread.setName("Server_process_" + adPInstanceId);

			final ProcessExecutionResult result = ProcessInfo.builder()
					.setAD_PInstance_ID(adPInstanceId)
					.setCreateTemporaryCtx()
					//
					.buildAndPrepareExecution()
					.switchContextWhenRunning()
					.executeSync()
					.getResult();

			return result;
		}
		finally
		{
			currentThread.setName(threadNameBkp);
		}
	}

	@Override
	public EMailSentStatus sendEMail(final EMail email)
	{
		return email.send();
	}
}
