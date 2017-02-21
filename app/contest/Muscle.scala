package me.michaelgagnon.pets.contest

import  me.michaelgagnon.pets.web.actors.Pet

object Muscle extends Contest {

  def getSummary(firstName: String, secondName: String): String =
    s"$firstName tossed $secondName out of bounds."

  def apply(pet1: Pet, pet2: Pet): ContestResult =

    if (pet1.strength >= pet2.strength) {
      ContestResult(pet1, pet2, Some(getSummary(pet1.name, pet2.name)))
    } else {
      ContestResult(pet2, pet1, Some(getSummary(pet2.name, pet1.name)))
    }
}
