package com.miya10kei.haribote

import com.github.ajalt.clikt.output.TermUi

fun TermUi.repeatUntilEnteringYes(message: String, action: () -> Unit) {
  while (true) {
    if (this.confirm(message) == true) {
      action()
      break
    }
  }
}
