package com.alipay.sofa.serverless.arklet.core.api;

import java.util.ArrayList;
import java.util.List;

import com.alipay.sofa.serverless.arklet.core.api.tunnel.Tunnel;
import com.alipay.sofa.serverless.arklet.core.api.tunnel.http.HttpTunnel;
import com.alipay.sofa.serverless.arklet.core.command.CommandService;
import com.alipay.sofa.serverless.arklet.core.ArkletComponent;
import com.google.inject.AbstractModule;
import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;

/**
 * @author mingmen
 * @date 2023/6/8
 */

@Singleton
public class ApiClient implements ArkletComponent {


    private static final List<Tunnel> tunnelList = new ArrayList<>(8);

    @Inject
    private CommandService commandService;

    static {
        Injector injector = Guice.createInjector(new TunnelGuiceModule());
        for (Binding<Tunnel> binding : injector
            .findBindingsByType(new TypeLiteral<Tunnel>() {
            })) {
            tunnelList.add(binding.getProvider().get());
        }
    }

    @Override
    public void init() {
        for (Tunnel tunnel : tunnelList) {
            tunnel.init(commandService);
            tunnel.run();
        }
    }

    @Override
    public void destroy() {
        for (Tunnel tunnel : tunnelList) {
            tunnel.shutdown();
        }
    }

    public List<Tunnel> getTunnels() {
        return tunnelList;
    }

    private static class TunnelGuiceModule extends AbstractModule {
        @Override
        protected void configure() {
            Multibinder<Tunnel> tunnelMultibinder = Multibinder.newSetBinder(binder(),
                Tunnel.class);
            tunnelMultibinder.addBinding().to(HttpTunnel.class);
        }
    }

}
