package com.mariadb.columnstoredemo;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@Push
@SpringBootApplication
public class ColumnstoredemoApplication implements AppShellConfigurator {

	public static void main(String[] args) {
		System.setProperty("reactor.schedulers.defaultBoundedElasticOnVirtualThreads", "true");
		SpringApplication.run(ColumnstoredemoApplication.class, args);
	}

}
