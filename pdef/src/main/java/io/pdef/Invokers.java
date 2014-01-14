/*
 * Copyright: 2013 Pdef <http://pdef.io/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.pdef;

public class Invokers {
	private Invokers () {}

	/**
	 * Creates an invoker which uses a service to execute invocations.
	 */
	public static <T> Invoker of(final T service) {
		return of(Providers.ofInstance(service));
	}

	/**
	 * Creates an invoker which uses a service provider to execute invocations.
	 */
	public static <T> Invoker of(final Provider<T> provider) {
		if (provider == null) throw new NullPointerException("provider");
		return new Invoker() {
			@Override
			public Object invoke(final Invocation invocation) throws Exception {
				T service = provider.get();
				return invocation.invoke(service);
			}
		};
	}
}
