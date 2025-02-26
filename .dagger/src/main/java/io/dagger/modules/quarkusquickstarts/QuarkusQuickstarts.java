package io.dagger.modules.quarkusquickstarts;

import io.dagger.client.*;
import io.dagger.module.AbstractModule;
import io.dagger.module.annotation.Function;
import io.dagger.module.annotation.Object;
import java.util.List;
import java.util.concurrent.ExecutionException;

/** QuarkusQuickstarts main object */
@Object
public class QuarkusQuickstarts extends AbstractModule {
  @Function
  public Service app(Directory src)
      throws ExecutionException, DaggerQueryException, InterruptedException {
    Directory generatedApp =
        dag.java()
            // inside a GraalVM container
            .graalvm()
            // with the application sources
            .withSources(src)
            // run the install maven command
            // -> this uses by default the mvnw wrapper
            .withMvnExec(List.of("install"))
            // and return the directory with the application built
            .directory("target/quarkus-app");

    Service svc =
        dag.java()
            // inside a JRE container
            .jre()
            // copy the directory we just build
            .withAppDirectory(generatedApp)
            // set the working directory as the application directory
            .withAppWorkdir()
            // expose port 8080 to the host
            .withExposedPort(8080)
            // define the command to be run as a service
            .customRunAsService(List.of("java", "-jar", "quarkus-run.jar"));

    return svc;
  }

  @Function
  public Directory dir(Directory src)
      throws ExecutionException, DaggerQueryException, InterruptedException {
    return dag.java()
        // inside a GraalVM container
        .graalvm()
        // with the application sources
        .withSources(src)
        // run the install maven command
        // -> this uses by default the mvnw wrapper
        .withMvnExec(List.of("install"))
        // and return the directory with the application built
        .directory("target/quarkus-app");
  }

  @Function
  public Service svc(Directory src)
      throws ExecutionException, DaggerQueryException, InterruptedException {
    return dag.java()
        // inside a JRE container
        .jre()
        // copy the directory we just build
        .withAppDirectory(dir(src))
        // set the working directory as the application directory
        .withAppWorkdir()
        // expose port 8080 to the host
        .withExposedPort(8080)
        // define the command to be run as a service
        .customRunAsService(List.of("java", "-jar", "quarkus-run.jar"));
  }

  @Function
  public Service run(Directory src)
      throws ExecutionException, DaggerQueryException, InterruptedException {
    return jreWithApp(src)
        .withExposedPort(8080)
        .customRunAsService(List.of("java", "-jar", "quarkus-run.jar"));
  }

  @Function
  public Container container(Directory src)
      throws ExecutionException, DaggerQueryException, InterruptedException {
    return jreWithApp(src).container();
  }

  private JavaJre jreWithApp(Directory src) {
    return dag.java().jre().withAppDirectory(quarkusApp(src)).withAppWorkdir();
  }

  public Directory quarkusApp(Directory src) {
    return installApp(src).container().directory("target/quarkus-app");
  }

  private JavaMaven installApp(Directory src) {
    return dag.java().graalvm().withSources(src).withMvnExec(List.of("install"));
  }
}
