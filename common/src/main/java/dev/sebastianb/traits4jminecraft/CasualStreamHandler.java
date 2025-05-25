/*
 * Copyright 2019 Chocohead
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package dev.sebastianb.traits4jminecraft;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.security.Permission;
import java.util.Collections;
import java.util.Map;
import java.util.function.BiConsumer;

// copied from fabric asm
// thanks fabric asm for the inspiration
public final class CasualStreamHandler extends URLStreamHandler {
	private static final class CasualConnection extends URLConnection {
		private final byte[] realStream;

		public CasualConnection(URL url, byte[] realStream) {
			super(url);

			this.realStream = realStream;
		}

		@Override
		public void connect() throws IOException {
			System.out.println("Connection attempt");
			throw new UnsupportedOperationException();
		}

		@Override
		public InputStream getInputStream() {
			if (dumper == null) {
				System.err.println("Asked for " + url.getPath() + " too early to export");
			} else {
				System.out.println("Exporting " + url.getPath());
				dumper.accept(url.getPath().substring(1, url.getPath().length() - 6).replace('/', '.'), realStream);
			}
			return new ByteArrayInputStream(realStream);
		}

		@Override
		public Permission getPermission() {
			return null;
		}
	}

	static BiConsumer<String, byte[]> dumper;
	private final Map<String, byte[]> providers;

	public static URL create(String name, byte[] stream) {
		return create(Collections.singletonMap('/' + name.replace('.', '/') + ".class", stream));
	}

	public static URL create(Map<String, byte[]> mixins) {
		System.out.println("LOADED!");
		System.out.println(mixins);
		try {
			return new URL("traits4jminecraft", null, -1, "/", new CasualStreamHandler(mixins));
		} catch (MalformedURLException e) {
			throw new RuntimeException("Unexpected error creating URL", e);
		}
	}

	//There is a proper way to do this too https://stackoverflow.com/questions/26363573/registering-and-using-a-custom-java-net-url-protocol
	//Unfortunately the proper way requires being present on the system classloader, which we're not going to be :|
	public CasualStreamHandler(Map<String, byte[]> providers) {
		this.providers = providers;
	}

	@Override
	protected URLConnection openConnection(URL url) throws IOException {
		//System.out.println(providers.keySet());
		// System.out.println("Open connection on " + url.getPath());

		// TODO: we want to redirect all variables of the actual "logical class" to the "defined /gen/mixin/"
		// not sure if this works still but I'm trying

		// FIXME: so for some reason, it doesn't load because it can't find "/dev/sebastianb/traits4jminecraft/trait/MinecraftTestTrait"
		/*
		Exporting /dev/sebastianb/traits4jminecraft/gen/mixin/MinecraftTestTrait.class
		[17:52:08] [main/WARN] (FabricLoader/Mixin) Error loading class: /dev/sebastianb/traits4jminecraft/trait/MinecraftTestTrait (java.lang.ClassNotFoundException: /dev/sebastianb/traits4jminecraft/trait/MinecraftTestTrait)
		[17:52:08] [main/WARN] (FabricLoader/Mixin) @Mixin target .dev.sebastianb.traits4jminecraft.trait.MinecraftTestTrait was not found traits4jminecraft.mixins.json:MinecraftTestTrait from mod traits4jminecraft

		*/

		// the following code "fixes" that but preapply doesn't apply for mixin asm stuff

//		if (url.getPath().toString().equals("/dev/sebastianb/traits4jminecraft/trait/MinecraftTestTrait.class")) {
//
//			// print stacktrace
//			new Throwable().printStackTrace();
//
//			System.out.println("redirected");
//			// return "/dev/sebastianb/traits4jminecraft/gen/mixin/MinecraftTestTrait.class"
//			return new CasualConnection(url, providers.get("/dev/sebastianb/traits4jminecraft/gen/mixin/MinecraftTestTrait.class"));
//		}

		if (!providers.containsKey(url.getPath())) return null; //Who?
		System.out.println("### PASSED ###");
		return new CasualConnection(url, providers.get(url.getPath()));
	}
}