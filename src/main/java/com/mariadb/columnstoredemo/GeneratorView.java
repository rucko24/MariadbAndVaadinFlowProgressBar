package com.mariadb.columnstoredemo;

import com.mariadb.columnstoredemo.service.GeneratorService;
import com.mariadb.columnstoredemo.service.LoggingTotalTimeService;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.UIDetachedException;
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
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.vaadin.flow.theme.lumo.LumoUtility.JustifyContent;
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
    private Integer batchSizeValue;
    private Integer batchesValue;

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
        flexButtons.addClassNames(LumoUtility.FlexDirection.ROW, Top.MEDIUM, JustifyContent.BETWEEN);

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
        this.batchSizeValue = batchSize.getValue();
        this.batchesValue = batches.getValue();
        if (batchesValue != null && batchSizeValue != null) {
            progressBar.setVisible(true);
            start.setEnabled(false);
            final var stopWatch = new StopWatch();
            stopWatch.start();
            Mono.fromRunnable(() -> service.generate(batchSizeValue, batchesValue))
                    .subscribeOn(Schedulers.boundedElastic())
                    .doOnError(onError -> this.onError(ui, stopWatch))
                    .doOnTerminate(() -> this.onTerminated(ui, stopWatch))
                    .subscribe();
        } else {
            Notification.show("Bad inputs");
        }
    }

    private void onTerminated(final UI ui, StopWatch stopWatch) {
        try {
            ui.access(() -> this.loggingTotalTime(stopWatch, false));
        } catch (UIDetachedException ex) {
            this.loggingTotalTime(stopWatch, true);
        }
    }

    private void onError(final UI ui, StopWatch stopWatch) {
        log.error("Error {}");
        try {
            ui.access(() -> this.loggingTotalTime(stopWatch, false));
        } catch (UIDetachedException ex) {
            this.loggingTotalTime(stopWatch, true);
        }
    }

    private void loggingTotalTime(StopWatch stopWatch, boolean detachedUI) {
        if (!detachedUI) {
            progressBar.setVisible(false);
            start.setEnabled(true);
            Notification.show("Data generated.");
        }
        stopWatch.stop();
        var ms = Math.round(stopWatch.getTotalTimeMillis());
        var sec = Math.round(stopWatch.getTotalTimeSeconds());
        var min = Math.round(stopWatch.getTotalTime(TimeUnit.MINUTES));
        log.info("Total time {}min {}sec {}ms", min, sec, ms);
        var totalTime = min + "min " + sec + "sec " + ms + "ms";

        this.loggingTotalTimeService.loggingTotalTime(batchSizeValue, batchesValue, totalTime);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        if (attachEvent.isInitialAttach()) {
            var ui = attachEvent.getUI();
            this.executeService(ui);
        }
    }
}
