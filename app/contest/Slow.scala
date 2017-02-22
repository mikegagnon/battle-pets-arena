package me.michaelgagnon.pets.contest

import java.lang.Thread

import  me.michaelgagnon.pets.web.actors.Pet

object Slow extends Contest {

  def sleepMilli = 5000

  def apply(pet1: Pet, pet2: Pet): ContestResult = {

    Thread.sleep(sleepMilli)

    ContestResult(pet1, pet2, None)
  }

}
