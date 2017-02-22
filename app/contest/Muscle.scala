package me.michaelgagnon.pets.contest

import  me.michaelgagnon.pets.web.actors.Pet

object Muscle extends Contest {

  def getSummary(firstName: String, secondName: String): String =
    s"$firstName tossed $secondName out of bounds."

  def apply(pet1: Pet, pet2: Pet): ContestResult = {

    val (petA, petB) =
      if (pet1.strength >= pet2.strength) {
        (pet1, pet2)
      } else {
        (pet2, pet1)
      }

    ContestResult(petA, petB, Some(getSummary(petA.name, petB.name)))
  }
}
