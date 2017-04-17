package com.twoheart.dailyhotel.network.factory;

import com.daily.base.util.DailyTextUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executor;

import okhttp3.Request;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class TagCancellableCallAdapterFactory extends CallAdapter.Factory
{
    // References to the last Call made for a given tag
    final HashMap<Call, String> mQueuedCalls;
    final static Object mObject = new Object();

    private TagCancellableCallAdapterFactory()
    {
        mQueuedCalls = new HashMap<>();
    }

    public static TagCancellableCallAdapterFactory create()
    {
        return new TagCancellableCallAdapterFactory();
    }

    Type getCallResponseType(Type returnType)
    {
        if (returnType instanceof ParameterizedType == false)
        {
            throw new IllegalArgumentException("returnType instanceof ParameterizedType == false");
        }
        return getParameterUpperBound(0, (ParameterizedType) returnType);
    }

    @Override
    public CallAdapter<?, ?> get(Type returnType, Annotation[] annotations, Retrofit retrofit)
    {
        if (getRawType(returnType) != Call.class)
        {
            return null;
        }

        final Executor callbackExecutor = retrofit.callbackExecutor();
        final Type responseType = getCallResponseType(returnType);

        return new CallAdapter<Object, Call<?>>()
        {
            @Override
            public Type responseType()
            {
                return responseType;
            }

            @Override
            public Call<Object> adapt(Call<Object> call)
            {
                return new ExecutorCallbackCall<>(callbackExecutor, call, null, mQueuedCalls);
            }
        };
    }

    public void cancelAll(String tag)
    {
        if (DailyTextUtils.isTextEmpty(tag) == true)
        {
            return;
        }

        synchronized (mObject)
        {
            Iterator<Map.Entry<Call, String>> iterator = mQueuedCalls.entrySet().iterator();

            Call call;
            Map.Entry<Call, String> entry;

            while (iterator.hasNext() == true)
            {
                entry = iterator.next();

                if (tag.equalsIgnoreCase(entry.getValue()) == true)
                {
                    call = entry.getKey();

                    if (call != null)
                    {
                        call.cancel();
                    }

                    iterator.remove();
                }
            }
        }
    }

    public static final class ExecutorCallbackCall<T> implements Call<T>
    {
        private static final int RETRY_COUNT = 3;
        private int mRetryCount = 0;

        final Executor mCallbackExecutor;
        private final Call<T> mDelegate;
        private String mTag;
        final HashMap<Call, String> mQueuedCalls;

        ExecutorCallbackCall(Executor callbackExecutor, Call<T> delegate, String tag, HashMap<Call, String> queuedCalls)
        {
            mCallbackExecutor = callbackExecutor;
            mDelegate = delegate;
            mTag = tag;
            mQueuedCalls = queuedCalls;

            setTag(tag);
        }

        public void setTag(String tag)
        {
            if (DailyTextUtils.isTextEmpty(tag) == true)
            {
                return;
            }

            mTag = tag;

            synchronized (mObject)
            {
                mQueuedCalls.put(this, tag);
            }
        }

        @Override
        public void enqueue(final Callback<T> callback)
        {
            if (callback == null)
            {
                throw new NullPointerException("callback == null");
            }

            mDelegate.enqueue(new Callback<T>()
            {
                @Override
                public void onResponse(Call<T> call, final Response<T> response)
                {
                    synchronized (mObject)
                    {
                        mQueuedCalls.remove(ExecutorCallbackCall.this);
                    }

                    mCallbackExecutor.execute(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if (isCanceled() == false)
                            {
                                callback.onResponse(ExecutorCallbackCall.this, response);
                            }
                        }
                    });
                }

                @Override
                public void onFailure(Call<T> call, final Throwable t)
                {
                    synchronized (mObject)
                    {
                        mQueuedCalls.remove(ExecutorCallbackCall.this);
                    }

                    if (mRetryCount++ < RETRY_COUNT)
                    {
                        call.clone().enqueue(this);
                        return;
                    }

                    mCallbackExecutor.execute(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if (isCanceled() == false)
                            {
                                callback.onFailure(ExecutorCallbackCall.this, t);
                            }
                        }
                    });
                }
            });
        }

        @Override
        public boolean isExecuted()
        {
            return mDelegate.isExecuted();
        }

        @Override
        public Response<T> execute() throws IOException
        {
            return mDelegate.execute();
        }

        @Override
        public void cancel()
        {
            mDelegate.cancel();
        }

        @Override
        public boolean isCanceled()
        {
            return mDelegate.isCanceled();
        }

        @SuppressWarnings("CloneDoesntCallSuperClone") // Performing deep clone.
        @Override
        public Call<T> clone()
        {
            return new ExecutorCallbackCall<>(mCallbackExecutor, mDelegate.clone(), mTag, mQueuedCalls);
        }

        @Override
        public Request request()
        {
            return mDelegate.request();
        }
    }
}
