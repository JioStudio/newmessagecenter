package com.allstar.nmsc;

import com.allstar.cintracer.CinTracer;
import com.allstar.cintransaction.CinTransaction;
import com.allstar.cintransaction.CinTransactionCreated;
import com.allstar.event.handler.CinEventHandler;
import com.allstar.event.handler.CinEventHandlerManager;

public class TransactionCreated implements CinTransactionCreated
{
	CinTracer tracer = CinTracer.getInstance(TransactionCreated.class);

	@Override
	public void onTransactionCreated(CinTransaction trans)
	{
		try
		{
			CinEventHandler handler = CinEventHandlerManager.getHandler(trans);
			handler.handle();
		}
		catch (Exception e)
		{
			tracer.error("TransactionCreated Exception:", e);
		}
	}
}
