# What will Alejandro think of this progressbar ? LMAO

Inspiration from here

- https://programmingbrain.com/2021/09/testing-mariadb-columnstore-performance

Here we will see how to add a progress bar when we have a background process.

To execute that process and not block the UI we use in this example project-reactor with the Scheduler boundedElastic.

JDK 21 already allows to use project loom as well so we can combine them, as well as batman and superman, although we won't focus on increasing the performance either.

More detail. https://rubn0x52.com/2023/12/18/mariadb-con-vaadin-flow/