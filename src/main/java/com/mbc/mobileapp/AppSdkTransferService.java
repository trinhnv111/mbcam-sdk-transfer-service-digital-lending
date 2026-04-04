
package com.mbc.mobileapp;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.TimeZone;

import javax.annotation.PostConstruct;

import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
//import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.mbc.common.process.ESBLogProcess;
import com.mbc.common.process.TransactionLogProcess;
import com.mbc.common.queue.ThreadRunConfig;

@SpringBootApplication
// @EnableCircuitBreaker
// @EnableDiscoveryClient
// @EnableFeignClients
@EnableTransactionManagement
@EntityScan("com.mbc.*")
@EnableJpaRepositories(basePackages = "com.mbc.**", entityManagerFactoryRef = "entityManagerFactory", transactionManagerRef = "transactionManager")
@ComponentScan(basePackages = "com.*")
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT10S")
public class AppSdkTransferService extends SpringBootServletInitializer {

    @PostConstruct
    void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
    }

    public static void main(String[] args) throws Exception {
        String localIp = getLocalIp();
        System.setProperty("localIpValue", localIp);
        System.setProperty("oracle.net.disableOob", "true");
        ApplicationContext applicationContext = SpringApplication.run(AppSdkTransferService.class, args);

        ESBLogProcess esbThread;
        for (int i = 0; i < ThreadRunConfig.ESB_LOG; i++) {
            esbThread = applicationContext.getBean(ESBLogProcess.class);
            esbThread.setName("ESBLogProcess-thread-number-" + i);
            esbThread.start();

        }

        TransactionLogProcess transactionLogProcess;
        for (int i = 0; i < ThreadRunConfig.TRANSACTON_LOG; i++) {
            transactionLogProcess = applicationContext.getBean(TransactionLogProcess.class);
            transactionLogProcess.setName("TransactionLogProcess-thread-number-" + i);
            transactionLogProcess.start();
        }

//        KibanaLogProcess kibanaLogProcess;
//        for (int i = 0; i < ThreadRunConfig.KIBANA_LOG; i++) {
//            kibanaLogProcess = new KibanaLogProcess("KibanaLogProcess-" + i);
//            kibanaLogProcess.start();
//        }

//        DataCachedCollection datacache = new DataCachedCollection();
//        datacache.load();
    }

    public static String getLocalIp() {
        InetAddress result = null;
        try {
            int lowest = Integer.MAX_VALUE;
            for (Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces(); nics
                .hasMoreElements();) {
                NetworkInterface ifc = nics.nextElement();
                if (ifc.isUp()) {
                    if (ifc.getIndex() < lowest || result == null) {
                        lowest = ifc.getIndex();
                    }
                    else if (result != null) {
                        continue;
                    }

                    // @formatter:off
                    // if (!ignoreInterface(ifc.getDisplayName())) {
                    for (Enumeration<InetAddress> addrs = ifc.getInetAddresses(); addrs.hasMoreElements();) {
                        InetAddress address = addrs.nextElement();
                        if (address instanceof Inet4Address && !address.isLoopbackAddress()) {
                            result = address;
                        }
                    }
                    // }
                    // @formatter:on
                }
            }
        }
        catch (IOException ex) {

        }

        if (result != null) {
            return result.getHostAddress();
        }

        try {
            return InetAddress.getLocalHost().getHostAddress();
        }
        catch (UnknownHostException e) {

        }

        return null;
    }

    // private static boolean isExistActiveProfile(String[] args) {
    // boolean result = false;
    // for (String arg : args) {
    // if (arg.contains("--spring.profiles.active")) {
    // result = true;
    // break;
    // }
    // }
    // return result;
    // }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {

            System.out.println("Let's inspect the beans provided by Spring Boot:");

            String[] beanNames = ctx.getBeanDefinitionNames();
            Arrays.sort(beanNames);
            for (String beanName : beanNames) {
                System.out.println(beanName);
            }
        };
    }

    @Bean
    public WebMvcConfigurer redirectToIndex() {
        return new WebMvcConfigurer() {

            @Override
            public void addViewControllers(ViewControllerRegistry registry) {
                // redirect requests to / to swagger-ui
                registry.addRedirectViewController("/", "/swagger-ui.html");
            }
        };
    }
}
