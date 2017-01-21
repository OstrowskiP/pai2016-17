package pl.lodz.p.edu.dao

import pl.lodz.p.edu.model.Window

trait WindowsDao {
  def create(window: Window): String

  def update(window: Window): Boolean

  def findByName(window: Window): Window

  def findById(windowId: Int): Window
}


