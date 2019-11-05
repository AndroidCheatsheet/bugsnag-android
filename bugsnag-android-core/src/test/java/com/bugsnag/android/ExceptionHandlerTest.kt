package com.bugsnag.android

import org.junit.After
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner
import java.lang.Thread

@RunWith(MockitoJUnitRunner::class)
internal class ExceptionHandlerTest {

    @Mock
    lateinit var client: Client

    @Mock
    lateinit var notifyDelegate: NotifyDelegate

    var originalHandler: Thread.UncaughtExceptionHandler? = null

    @Before
    fun setUp() {
        `when`(client.getNotifyDelegate()).thenReturn(notifyDelegate)
        originalHandler = Thread.getDefaultUncaughtExceptionHandler()
    }

    @After
    fun tearDown() {
        Thread.setDefaultUncaughtExceptionHandler(originalHandler)
    }

    @Test
    fun handlerInstalled() {
        val exceptionHandler = ExceptionHandler(client, NoopLogger)
        assertSame(exceptionHandler, Thread.getDefaultUncaughtExceptionHandler())
    }

    @Test
    fun uncaughtException() {
        val exceptionHandler = ExceptionHandler(client, NoopLogger)
        val thread = Thread.currentThread()
        val exc = RuntimeException("Whoops")
        exceptionHandler.uncaughtException(thread, exc)
        verify(notifyDelegate, times(1)).notifyUnhandledException(
            exc,
            HandledState.REASON_UNHANDLED_EXCEPTION,
            null,
            thread
        )
    }

    @Test
    fun exceptionPropagated() {
        var propagated = false
        Thread.setDefaultUncaughtExceptionHandler { _, _ -> propagated = true }
        val exceptionHandler = ExceptionHandler(client, NoopLogger)
        val thread = Thread.currentThread()
        exceptionHandler.uncaughtException(thread, RuntimeException("Whoops"))
        assertTrue(propagated)
    }
}
