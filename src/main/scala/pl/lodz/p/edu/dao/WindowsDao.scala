package pl.lodz.p.edu.dao

import pl.lodz.p.edu.model.Window

trait WindowsDao {
  def create(window: Window): String

  def findByName(window: Window): Window

  def update(window: Window): Boolean
}



