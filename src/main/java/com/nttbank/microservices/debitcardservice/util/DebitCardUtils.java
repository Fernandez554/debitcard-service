package com.nttbank.microservices.debitcardservice.util;

import com.nttbank.microservices.debitcardservice.model.entity.DebitCard;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class DebitCardUtils {

  public static <T> void addElementToSet(DebitCard debitCard, T element,
      Function<DebitCard, Set<T>> getter, BiConsumer<DebitCard, Set<T>> setter) {
    Set<T> set = Optional.ofNullable(getter.apply(debitCard))
        .orElseGet(() -> {
          Set<T> newSet = new HashSet<>();
          setter.accept(debitCard, newSet);
          return newSet;
        });
    set.add(element);
    setter.accept(debitCard, set);
  }

  public static <T> void removeElementToSet(DebitCard debitCard, T element,
      Function<DebitCard, Set<T>> getter, BiConsumer<DebitCard, Set<T>> setter) {
    Set<T> set = Optional.ofNullable(getter.apply(debitCard))
        .orElseGet(() -> {
          Set<T> newSet = new HashSet<>();
          setter.accept(debitCard, newSet);
          return newSet;
        });
    set.remove(element);
    setter.accept(debitCard, set);
  }

}
