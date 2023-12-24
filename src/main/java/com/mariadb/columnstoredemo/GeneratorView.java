package com.mariadb.columnstoredemo;

import com.mariadb.columnstoredemo.service.GeneratorService;
import com.mariadb.columnstoredemo.service.LoggingTotalTimeService;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.FlexLayout.FlexDirection;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin.Top;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.StopWatch;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.TimeUnit;

@Route("")
@PageTitle("Data generator")
@Log4j2
public class GeneratorView extends VerticalLayout {

    private final ProgressBar progressBar = new ProgressBar();
    private final IntegerField batchSize = new IntegerField("Batch size");
    private final IntegerField batches = new IntegerField("Batches");
    private final Button start = new Button("Start");
    private final Button clear = new Button("Clear", VaadinIcon.TRASH.create());
    private final GeneratorService service;
    private final LoggingTotalTimeService loggingTotalTimeService;

    public GeneratorView(GeneratorService service, LoggingTotalTimeService loggingTotalTimeService) {
        this.service = service;
        this.loggingTotalTimeService = loggingTotalTimeService;

        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        clear.addThemeVariants(ButtonVariant.LUMO_ERROR);

        final FlexLayout flexLayout = new FlexLayout();
        flexLayout.setId("button-zone");
        flexLayout.setFlexDirection(FlexDirection.COLUMN);

        final FlexLayout flexButtons = new FlexLayout(start, clear);
        flexButtons.setFlexDirection(FlexDirection.ROW);
        flexButtons.addClassNames(Top.MEDIUM);
        flexButtons.setJustifyContentMode(JustifyContentMode.BETWEEN);

        final FlexLayout flexForProgressBar = new FlexLayout(progressBar);

        flexLayout.add(batchSize, batches, flexButtons, flexForProgressBar);

        add(new H1("Data generator"), flexLayout);
    }

    private void executeService(final UI ui) {

        clear.addClickListener(buttonClickEvent -> {
            batches.clear();
            batchSize.clear();
        });

        start.addClickListener(event -> this.process(ui));

    }

    private void process(final UI ui) {
        if (batchSize.getValue() != null && batches.getValue() != null) {
            progressBar.setVisible(true);
            start.setEnabled(false);
            final var stopWatch = new StopWatch();
            stopWatch.start();
            Mono.fromRunnable(() -> service.generate(batchSize.getValue(), batches.getValue()))
                    .subscribeOn(Schedulers.boundedElastic())
                    .doOnError(onError -> log.error("Error {}", onError))
                    .doOnTerminate(() -> this.onTerminated(ui, stopWatch))
                    .subscribe();
        } else {
            Notification.show("Bad inputs");
        }
    }

    private void onTerminated(final UI ui, StopWatch stopWatch) {
        ui.access(() -> {
            progressBar.setVisible(false);
            start.setEnabled(true);
            Notification.show("Data generated.");
            stopWatch.stop();
            var ms = Math.round(stopWatch.getTotalTimeMillis());
            var sec = Math.round(stopWatch.getTotalTimeSeconds());
            var min = Math.round(stopWatch.getTotalTime(TimeUnit.MINUTES));
            log.info("Total time {}min {}sec {}ms", min, sec, ms);
            var totalTime = min + "min " + sec + "sec " + ms + "ms";
            this.loggingTotalTime(totalTime);
        });
    }

    private void loggingTotalTime(String totalTimeResult) {
        var bathSize = batchSize.getValue();
        var bacthes = batches.getValue();
        this.loggingTotalTimeService.loggingTotalTime(bathSize, bacthes, totalTimeResult);
    }


    @Override
    protected void onAttach(AttachEvent attachEvent) {
        if (attachEvent.isInitialAttach()) {
            var ui = attachEvent.getUI();
            this.executeService(ui);
        }
    }
}
