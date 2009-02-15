package org.unseen.guice.composite.scopes.test;

import static com.google.inject.Guice.createInjector;
import static com.google.inject.name.Names.named;
import static java.awt.Color.BLACK;
import static java.awt.Color.BLUE;
import static java.awt.Color.GRAY;
import static java.awt.Color.GREEN;
import static java.awt.Color.ORANGE;
import static java.awt.Color.RED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.awt.Color;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.Ignore;
import org.junit.Test;
import org.unseen.guice.composite.scopes.Arg;
import org.unseen.guice.composite.scopes.binder.DynamicScopesModule;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Named;

/**
 * These are adaptations of the AssitedInject tests
 * 
 * @author Todor Boev
 */
public class ClassScopeTests {

  @Test
  public void testAssistedFactory() {
    Injector inj = createInjector(new DynamicScopesModule() {
      @Override
      protected void configure() {
        bind(Double.class).toInstance(5.0d);
        bind(ColoredCarFactory.class).toClassScope(Mustang.class);
      }
    });
    ColoredCarFactory carFactory = inj.getInstance(ColoredCarFactory.class);

    Mustang blueMustang = (Mustang) carFactory.create(BLUE);
    assertEquals(BLUE, blueMustang.color);
    assertEquals(5.0d, blueMustang.engineSize);

    Mustang redMustang = (Mustang) carFactory.create(RED);
    assertEquals(RED, redMustang.color);
    assertEquals(5.0d, redMustang.engineSize);
  }

  @Test
  public void testAssistedFactoryWithAnnotations() {
    Injector inj = createInjector(new DynamicScopesModule() {
      @Override
      protected void configure() {
        bind(int.class).annotatedWith(named("horsePower")).toInstance(250);
        bind(int.class).annotatedWith(named("modelYear")).toInstance(1984);
        bind(ColoredCarFactory.class).toClassScope(Camaro.class);
      }
    });

    ColoredCarFactory carFactory = inj.getInstance(ColoredCarFactory.class);

    Camaro blueCamaro = (Camaro) carFactory.create(BLUE);
    assertEquals(BLUE, blueCamaro.color);
    assertEquals(1984, blueCamaro.modelYear);
    assertEquals(250, blueCamaro.horsePower);

    Camaro redCamaro = (Camaro) carFactory.create(RED);
    assertEquals(RED, redCamaro.color);
    assertEquals(1984, redCamaro.modelYear);
    assertEquals(250, redCamaro.horsePower);
  }

  interface Car {}

  interface ColoredCarFactory {
    Car create(Color color);
  }

  public static class Mustang implements Car {
    private final double engineSize;
    private final Color color;

    @Inject
    public Mustang(double engineSize, @Arg Color color) {
      this.engineSize = engineSize;
      this.color = color;
    }

    public void drive() {
    }
  }

  public static class Camaro implements Car {
    private final int horsePower;
    private final int modelYear;
    private final Color color;

    @Inject
    public Camaro(@Named("horsePower") int horsePower, @Named("modelYear") int modelYear,
        @Arg Color color) {
      this.horsePower = horsePower;
      this.modelYear = modelYear;
      this.color = color;
    }
  }

  interface SummerCarFactory {
    Car create(Color color, boolean convertable);
  }

  @Test
  public void testFactoryUsesInjectedConstructor() {
    Injector inj = createInjector(new DynamicScopesModule() {
      @Override
      protected void configure() {
        bind(float.class).toInstance(140f);
        bind(SummerCarFactory.class).toClassScope(Corvette.class);
      }
    });

    SummerCarFactory carFactory = inj.getInstance(SummerCarFactory.class);

    Corvette redCorvette = (Corvette) carFactory.create(RED, false);
    assertEquals(RED, redCorvette.color);
    assertEquals(140f, redCorvette.maxMph);
    assertFalse(redCorvette.isConvertable);
  }

  public static class Corvette implements Car {
    private boolean isConvertable;
    private Color color;
    private float maxMph;

    public Corvette(Color color, boolean isConvertable) {
      throw new IllegalStateException("Not an @ArgInject constructor");
    }

    @Inject
    public Corvette(@Arg Color color, Float maxMph, @Arg boolean isConvertable) {
      this.isConvertable = isConvertable;
      this.color = color;
      this.maxMph = maxMph;
    }
  }

  @Test
  public void testConstructorDoesntNeedAllFactoryMethodArguments() {
    Injector inj = createInjector(new DynamicScopesModule() {
      protected void configure() {
        bind(SummerCarFactory.class).toClassScope(Beetle.class);
      }
    });
    SummerCarFactory factory = inj.getInstance(SummerCarFactory.class);

    Beetle beetle = (Beetle) factory.create(RED, true);
    assertSame(RED, beetle.color);
  }

  public static class Beetle implements Car {
    private final Color color;

    @Inject
    public Beetle(@Arg Color color) {
      this.color = color;
    }
  }

  @Test
  public void testMethodsAndFieldsGetInjected() {
    Injector inj = createInjector(new DynamicScopesModule() {
      @Override
      protected void configure() {
        bind(String.class).toInstance("turbo");
        bind(int.class).toInstance(911);
        bind(double.class).toInstance(50000d);
        bind(ColoredCarFactory.class).toClassScope(Porshe.class);
      }
    });
    ColoredCarFactory carFactory = inj.getInstance(ColoredCarFactory.class);

    Porshe grayPorshe = (Porshe) carFactory.create(GRAY);
    assertEquals(GRAY, grayPorshe.color);
    assertEquals(50000d, grayPorshe.price);
    assertEquals(911, grayPorshe.model);
    assertEquals("turbo", grayPorshe.name);
  }

  public static class Porshe implements Car {
    private final Color color;
    private final double price;
    private @Inject
    String name;
    private int model;

    @Inject
    public Porshe(@Arg Color color, double price) {
      this.color = color;
      this.price = price;
    }

    @Inject
    void setModel(int model) {
      this.model = model;
    }
  }

  @Test
  public void testProviderInjection() {
    Injector inj = createInjector(new DynamicScopesModule() {
      @Override
      protected void configure() {
        bind(String.class).toInstance("trans am");
        bind(ColoredCarFactory.class).toClassScope(Firebird.class);
      }
    });
    ColoredCarFactory carFactory = inj.getInstance(ColoredCarFactory.class);

    Firebird blackFirebird = (Firebird) carFactory.create(BLACK);
    assertEquals(BLACK, blackFirebird.color);
    assertEquals("trans am", blackFirebird.modifiersProvider.get());
  }

  public static class Firebird implements Car {
    private final Provider<String> modifiersProvider;
    private final Color color;

    @Inject
    public Firebird(Provider<String> modifiersProvider, @Arg Color color) {
      this.modifiersProvider = modifiersProvider;
      this.color = color;
    }
  }

  @Test
  public void testTypeTokenInjection() {
    Injector inj = createInjector(new DynamicScopesModule() {
      @Override
      protected void configure() {
        bind(new TypeLiteral<Set<String>>() {
        }).toInstance(Collections.singleton("Flux Capacitor"));
        bind(new TypeLiteral<Set<Integer>>() {
        }).toInstance(Collections.singleton(88));
        bind(ColoredCarFactory.class).toClassScope(DeLorean.class);
      }
    });
    ColoredCarFactory carFactory = inj.getInstance(ColoredCarFactory.class);

    DeLorean deLorean = (DeLorean) carFactory.create(GRAY);
    assertEquals(GRAY, deLorean.color);
    assertEquals("Flux Capacitor", deLorean.features.iterator().next());
    assertEquals(new Integer(88), deLorean.featureActivationSpeeds.iterator().next());
  }

  public static class DeLorean implements Car {
    private final Set<String> features;
    private final Set<Integer> featureActivationSpeeds;
    private final Color color;

    @Inject
    public DeLorean(Set<String> extraFeatures, Set<Integer> featureActivationSpeeds,
        @Arg Color color) {
      this.features = extraFeatures;
      this.featureActivationSpeeds = featureActivationSpeeds;
      this.color = color;
    }
  }

  @Test
  public void testTypeTokenProviderInjection() {
    Injector inj = createInjector(new DynamicScopesModule() {
      @Override
      protected void configure() {
        bind(new TypeLiteral<Set<String>>() {
        }).toInstance(Collections.singleton("Datsun"));
        bind(ColoredCarFactory.class).toClassScope(Z.class);
      }
    });
    ColoredCarFactory carFactory = inj.getInstance(ColoredCarFactory.class);

    Z orangeZ = (Z) carFactory.create(ORANGE);
    assertEquals(ORANGE, orangeZ.color);
    assertEquals("Datsun", orangeZ.manufacturersProvider.get().iterator().next());
  }

  public static class Z implements Car {
    private final Provider<Set<String>> manufacturersProvider;
    private final Color color;

    @Inject
    public Z(Provider<Set<String>> manufacturersProvider, @Arg Color color) {
      this.manufacturersProvider = manufacturersProvider;
      this.color = color;
    }
  }

  public static class Prius implements Car {
    final Color color;

    @Inject
    private Prius(@Arg Color color) {
      this.color = color;
    }
  }

  @Test
  public void testAssistInjectionInNonPublicConstructor() {
    Injector injector = createInjector(new DynamicScopesModule() {
      @Override
      protected void configure() {
        bind(ColoredCarFactory.class).toClassScope(Prius.class);
      }
    });
    Prius prius = (Prius) injector.getInstance(ColoredCarFactory.class).create(ORANGE);
    assertEquals(prius.color, ORANGE);
  }

  public static class ExplodingCar implements Car {
    @Inject
    public ExplodingCar(@Arg Color color) {
      throw new IllegalStateException("kaboom!");
    }
  }

  @Test
  public void testExceptionDuringConstruction() {
    Injector injector = createInjector(new DynamicScopesModule() {
      @Override
      protected void configure() {
        bind(ColoredCarFactory.class).toClassScope(ExplodingCar.class);
      }
    });
    try {
      injector.getInstance(ColoredCarFactory.class).create(ORANGE);
      fail();
    } catch (IllegalStateException e) {
      assertEquals("kaboom!", e.getMessage());
    }
  }

  public static class DefectiveCar implements Car {
    @Inject
    public DefectiveCar() throws ExplosionException, FireException {
      throw new ExplosionException();
    }
  }

  public static class ExplosionException extends Exception {}

  public static class FireException extends Exception {}

  public interface DefectiveCarFactoryWithNoExceptions {
    Car createCar();
  }

  public interface DefectiveCarFactory {
    Car createCar() throws FireException;
  }

  public interface CorrectDefectiveCarFactory {
    Car createCar() throws FireException, ExplosionException;
  }

  @Test
  public void testConstructorExceptionsAreThrownByFactory() {
    Injector injector = createInjector(new DynamicScopesModule() {
      @Override
      protected void configure() {
        bind(CorrectDefectiveCarFactory.class).toClassScope(DefectiveCar.class);
      }
    });
    try {
      injector.getInstance(CorrectDefectiveCarFactory.class).createCar();
      fail();
    } catch (FireException e) {
      fail();
    } catch (ExplosionException expected) {
    }
  }

  public static class WildcardCollection {

    public interface Factory {
      WildcardCollection create(Collection<?> items);
    }

    @Inject
    public WildcardCollection(@Arg Collection<?> items) {
    }
  }

  @Test
  public void testWildcardGenerics() {
    Injector injector = createInjector(new DynamicScopesModule() {
      @Override
      protected void configure() {
        bind(WildcardCollection.Factory.class).toClassScope(WildcardCollection.class);
      }
    });
    WildcardCollection.Factory factory = injector.getInstance(WildcardCollection.Factory.class);
    factory.create(Collections.emptyList());
  }

  public static class SteeringWheel {}

  public static class Fiat implements Car {
    private final SteeringWheel steeringWheel;
    private final Color color;

    @Inject
    public Fiat(SteeringWheel steeringWheel, @Arg Color color) {
      this.steeringWheel = steeringWheel;
      this.color = color;
    }
  }

  @Test
  public void testFactoryWithImplicitBindings() {
    Injector injector = createInjector(new DynamicScopesModule() {
      @Override
      protected void configure() {
        bind(ColoredCarFactory.class).toClassScope(Fiat.class);
      }
    });

    ColoredCarFactory coloredCarFactory = injector.getInstance(ColoredCarFactory.class);
    Fiat fiat = (Fiat) coloredCarFactory.create(GREEN);
    assertEquals(GREEN, fiat.color);
    assertNotNull(fiat.steeringWheel);
  }

// @Test
// public void testFactoryFailsWithMissingBinding() {
// try {
// createInjector(new DynamicScopesModule() {
// @Override
// protected void configure() {
// bind(ColoredCarFactory.class).toClassScope(Mustang.class);
// }
// });
// fail();
// } catch (CreationException expected) {
// assertContains(expected.getMessage(),
// "Could not find a suitable constructor in java.lang.Double.", "at "
// + ColoredCarFactory.class.getName() + ".create(FactoryProvider2Test.java");
// }
// }

// @Test
// public void testMethodsDeclaredInObject() {
// Injector injector = createInjector(new DynamicScopesModule() {
// @Override
// protected void configure() {
// bind(Double.class).toInstance(5.0d);
// bind(ColoredCarFactory.class).toClassScope(Mustang.class);
// }
// });
//
// ColoredCarFactory carFactory = injector.getInstance(ColoredCarFactory.class);
//
// assertEqualsBothWays(carFactory, carFactory);
// assertEquals(ColoredCarFactory.class.getName() + " for " +
  // Mustang.class.getName(), carFactory
// .toString());
// }

  static class Subaru implements Car {
    @Inject
    @Arg
    Provider<Color> colorProvider;
  }

  @Ignore @Test
  public void testInjectingProviderOfParameter() {
    Injector inj = createInjector(new DynamicScopesModule() {
      @Override
      protected void configure() {
        bind(ColoredCarFactory.class).toClassScope(Subaru.class);
      }
    });

    ColoredCarFactory carFactory = inj.getInstance(ColoredCarFactory.class);
    Subaru subaru = (Subaru) carFactory.create(RED);

    assertSame(RED, subaru.colorProvider.get());
    assertSame(RED, subaru.colorProvider.get());
  }

  @Ignore @Test
  public void testInjectingNullParameter() {
    Injector injector = createInjector(new DynamicScopesModule() {
      @Override
      protected void configure() {
        bind(ColoredCarFactory.class).toClassScope(Subaru.class);
      }
    });

    ColoredCarFactory carFactory = injector.getInstance(ColoredCarFactory.class);
    Subaru subaru = (Subaru) carFactory.create(null);

    assertNull(subaru.colorProvider.get());
    assertNull(subaru.colorProvider.get());
  }

// @Test
// public void testFactoryUseBeforeInitialization() {
// ColoredCarFactory carFactory = FactoryProvider
// .newFactory(ColoredCarFactory.class, Subaru.class).get();
// try {
// carFactory.create(RED);
// fail();
// } catch (IllegalStateException expected) {
// assertContains(expected.getMessage(),
// "Factories.create() factories cannot be used until they're initialized by Guice.");
// }
// }

  interface MustangFactory {
    Mustang create(Color color);
  }

  @Test
  public void testFactoryBuildingConcreteTypes() {
    Injector injector = createInjector(new DynamicScopesModule() {
      protected void configure() {
        bind(double.class).toInstance(5.0d);
        // note there is no 'thatMakes()' call here:
        bind(MustangFactory.class).toClassScope(Mustang.class);
      }
    });
    MustangFactory factory = injector.getInstance(MustangFactory.class);

    Mustang mustang = factory.create(RED);
    assertSame(RED, mustang.color);
    assertEquals(5.0d, mustang.engineSize);
  }

  static class Fleet {
    @Inject
    Mustang mustang;
    @Inject
    Camaro camaro;
  }

  interface FleetFactory {
    Fleet createFleet(Color color);
  }

  @Test
  public void testInjectDeepIntoConstructedObjects() {
    Injector injector = createInjector(new DynamicScopesModule() {
      @Override
      protected void configure() {
        bind(double.class).toInstance(5.0d);
        bind(int.class).annotatedWith(named("horsePower")).toInstance(250);
        bind(int.class).annotatedWith(named("modelYear")).toInstance(1984);
        bind(FleetFactory.class).toClassScope(Fleet.class);
      }
    });

    FleetFactory fleetFactory = injector.getInstance(FleetFactory.class);
    Fleet fleet = fleetFactory.createFleet(RED);

    assertSame(RED, fleet.mustang.color);
    assertEquals(5.0d, fleet.mustang.engineSize);
    assertSame(RED, fleet.camaro.color);
    assertEquals(250, fleet.camaro.horsePower);
    assertEquals(1984, fleet.camaro.modelYear);
  }

  interface TwoToneCarFactory {
    Car create(@Arg(name = "paint") Color paint, @Arg(name = "fabric") Color fabric);
  }

  static class Maxima implements Car {
    @Inject
    @Arg(name = "paint")
    Color paint;
    @Inject
    @Arg(name = "fabric")
    Color fabric;
  }

  public void testDistinctKeys() {
    Injector injector = createInjector(new DynamicScopesModule() {
      @Override
      protected void configure() {
        bind(TwoToneCarFactory.class).toClassScope(Maxima.class);
      }
    });

    TwoToneCarFactory factory = injector.getInstance(TwoToneCarFactory.class);
    Maxima maxima = (Maxima) factory.create(BLACK, GRAY);
    assertSame(BLACK, maxima.paint);
    assertSame(GRAY, maxima.fabric);
  }

  interface DoubleToneCarFactory {
    Car create(@Arg(name = "paint") Color paint, @Arg(name = "paint") Color morePaint);
  }

// @Test
// public void testDuplicateKeys() {
// try {
// createInjector(new DynamicScopesModule() {
// @Override
// protected void configure() {
// bind(DoubleToneCarFactory.class).toClassScope(Maxima.class);
// }
// });
// fail();
// } catch (CreationException expected) {
// assertContains(expected.getMessage(),
  // "A binding to java.awt.Color annotated with @"
// + Arg.class.getName() + "(value=paint) was already configured at");
// }
// }

  @Test
  public void testMethodInterceptorsOnAssistedTypes() {
    final AtomicInteger invocationCount = new AtomicInteger();
    final MethodInterceptor interceptor = new MethodInterceptor() {
      public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        invocationCount.incrementAndGet();
        return methodInvocation.proceed();
      }
    };

    Injector injector = createInjector(new DynamicScopesModule() {
      @Override
      protected void configure() {
        bindInterceptor(Matchers.any(), Matchers.any(), interceptor);
        bind(Double.class).toInstance(5.0d);
        bind(ColoredCarFactory.class).toClassScope(Mustang.class);
      }
    });

    ColoredCarFactory factory = injector.getInstance(ColoredCarFactory.class);
    Mustang mustang = (Mustang) factory.create(GREEN);
    assertEquals(0, invocationCount.get());
    mustang.drive();
    assertEquals(1, invocationCount.get());
  }

// /**
// * Our factories aren't reusable across injectors. Although this behaviour
  // isn't something we
// * like, I have a test case to make sure the error message is pretty.
// */
// public void testFactoryReuseErrorMessageIsPretty() {
// final Provider<ColoredCarFactory> factoryProvider
// = FactoryProvider.newFactory(ColoredCarFactory.class, Mustang.class);
//
// createInjector(new DynamicScopesModule() {
// @Override protected void configure() {
// bind(Double.class).toInstance(5.0d);
// bind(ColoredCarFactory.class).toProvider(factoryProvider);
// }
// });
//
// try {
// createInjector(new DynamicScopesModule() {
// @Override protected void configure() {
// bind(Double.class).toInstance(5.0d);
// bind(ColoredCarFactory.class).toProvider(factoryProvider);
// }
// });
// fail();
// } catch(CreationException expected) {
// assertContains(expected.getMessage(),
// "Factories.create() factories may only be used in one Injector!");
// }
// }

// @Test
// public void testNonAssistedFactoryMethodParameter() {
// try {
// FactoryProvider.newFactory(NamedParameterFactory.class, Mustang.class);
// fail();
// } catch(ConfigurationException expected) {
// assertContains(expected.getMessage(),
// "Only @Arg is allowed for factory parameters, but found @" +
  // Named.class.getName());
// }
// }

  interface NamedParameterFactory {
    Car create(@Named("seats") int seats, double engineSize);
  }

// public void testDefaultAssistedAnnotation() throws NoSuchFieldException {
// Assisted plainAssisted
// =
  // Subaru.class.getDeclaredField("colorProvider").getAnnotation(Assisted.class);
// assertEqualsBothWays(FactoryProvider2.DEFAULT_ANNOTATION, plainAssisted);
// assertEquals(FactoryProvider2.DEFAULT_ANNOTATION.toString(),
  // plainAssisted.toString());
// }

  interface GenericColoredCarFactory<T extends Car> {
    T create(Color color);
  }

  @Test
  public void testGenericAssistedFactory() {
    final TypeLiteral<GenericColoredCarFactory<Mustang>> mustangTypeLiteral = 
      new TypeLiteral<GenericColoredCarFactory<Mustang>>() {};
    final TypeLiteral<GenericColoredCarFactory<Camaro>> camaroTypeLiteral = 
      new TypeLiteral<GenericColoredCarFactory<Camaro>>() {};

    Injector inj = createInjector(new DynamicScopesModule() {
      @Override
      protected void configure() {
        bind(Double.class).toInstance(5.0d);
        bind(int.class).annotatedWith(named("horsePower")).toInstance(250);
        bind(int.class).annotatedWith(named("modelYear")).toInstance(1984);
        bind(mustangTypeLiteral).toClassScope(Mustang.class);
        bind(camaroTypeLiteral).toClassScope(Camaro.class);
      }
    });

    GenericColoredCarFactory<Mustang> mustangFactory = inj.getInstance(Key.get(mustangTypeLiteral));
    GenericColoredCarFactory<Camaro> camaroFactory = inj.getInstance(Key.get(camaroTypeLiteral));

    Mustang blueMustang = mustangFactory.create(BLUE);
    assertEquals(BLUE, blueMustang.color);
    assertEquals(5.0d, blueMustang.engineSize);

    Camaro redCamaro = camaroFactory.create(RED);
    assertEquals(RED, redCamaro.color);
    assertEquals(1984, redCamaro.modelYear);
    assertEquals(250, redCamaro.horsePower);
  }

  public interface Insurance<T extends Car> {}

  public static class MustangInsurance implements Insurance<Mustang> {
    private final double premium;
    private final double limit;

    @Inject
    public MustangInsurance(@Named("lowLimit") double limit, @Arg double premium) {
      this.premium = premium;
      this.limit = limit;
    }

    public void sell() {
    }
  }

  public static class CamaroInsurance implements Insurance<Camaro> {
    private final double premium;
    private final double limit;

    @Inject
    public CamaroInsurance(@Named("highLimit") double limit, @Arg double premium) {
      this.premium = premium;
      this.limit = limit;
    }

    public void sell() {
    }
  }

  public interface MustangInsuranceFactory {
    public Insurance<Mustang> create(double premium);
  }

  public interface CamaroInsuranceFactory {
    public Insurance<Camaro> create(double premium);
  }

  @Test
  public void testAssistedFactoryForConcreteType() {
    Injector injector = createInjector(new DynamicScopesModule() {
      @Override
      protected void configure() {
        bind(Double.class).annotatedWith(named("lowLimit")).toInstance(50000.0d);
        bind(Double.class).annotatedWith(named("highLimit")).toInstance(100000.0d);
        bind(MustangInsuranceFactory.class).toClassScope(MustangInsurance.class);
        bind(CamaroInsuranceFactory.class).toClassScope(CamaroInsurance.class);
      }
    });

    MustangInsuranceFactory mustangInsuranceFactory = injector
        .getInstance(MustangInsuranceFactory.class);
    CamaroInsuranceFactory camaroInsuranceFactory = injector
        .getInstance(CamaroInsuranceFactory.class);

    MustangInsurance mustangPolicy = (MustangInsurance) mustangInsuranceFactory.create(800.0d);
    assertEquals(800.0d, mustangPolicy.premium);
    assertEquals(50000.0d, mustangPolicy.limit);

    CamaroInsurance camaroPolicy = (CamaroInsurance) camaroInsuranceFactory.create(800.0d);
    assertEquals(800.0d, camaroPolicy.premium);
    assertEquals(100000.0d, camaroPolicy.limit);
  }

  public interface InsuranceFactory<T extends Car> {
    public Insurance<T> create(double premium);
  }

  @Test
  public void testAssistedFactoryForParameterizedType() {
    final TypeLiteral<InsuranceFactory<Mustang>> mustangInsuranceFactoryType = new TypeLiteral<InsuranceFactory<Mustang>>() {
    };
    final TypeLiteral<InsuranceFactory<Camaro>> camaroInsuranceFactoryType = new TypeLiteral<InsuranceFactory<Camaro>>() {
    };

    Injector injector = createInjector(new DynamicScopesModule() {
      @Override
      protected void configure() {
        bind(Double.class).annotatedWith(named("lowLimit")).toInstance(50000.0d);
        bind(Double.class).annotatedWith(named("highLimit")).toInstance(100000.0d);
        bind(mustangInsuranceFactoryType).toClassScope(MustangInsurance.class);
        bind(camaroInsuranceFactoryType).toClassScope(CamaroInsurance.class);
      }
    });

    InsuranceFactory<Mustang> mustangInsuranceFactory = injector.getInstance(Key
        .get(mustangInsuranceFactoryType));
    InsuranceFactory<Camaro> camaroInsuranceFactory = injector.getInstance(Key
        .get(camaroInsuranceFactoryType));

    MustangInsurance mustangPolicy = (MustangInsurance) mustangInsuranceFactory.create(800.0d);
    assertEquals(800.0d, mustangPolicy.premium);
    assertEquals(50000.0d, mustangPolicy.limit);

    CamaroInsurance camaroPolicy = (CamaroInsurance) camaroInsuranceFactory.create(800.0d);
    assertEquals(800.0d, camaroPolicy.premium);
    assertEquals(100000.0d, camaroPolicy.limit);
  }
}
