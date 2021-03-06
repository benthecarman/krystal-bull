package com.krystal.bull.gui.dialog

import com.krystal.bull.gui.home.InitEventParams
import com.krystal.bull.gui.{GlobalData, KrystalBullUtil}
import org.bitcoins.core.protocol.tlv.EnumEventDescriptorV0TLV
import scalafx.Includes._
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Node
import scalafx.scene.control._
import scalafx.scene.layout.{GridPane, HBox, VBox}
import scalafx.stage.Window

object CreateEnumEventDialog {

  def showAndWait(parentWindow: Window): Option[InitEventParams] = {
    val dialog = new Dialog[Option[InitEventParams]]() {
      initOwner(parentWindow)
      title = "Create Enum Event"
    }

    dialog.dialogPane().buttonTypes = Seq(ButtonType.OK, ButtonType.Cancel)
    dialog.dialogPane().stylesheets = GlobalData.currentStyleSheets
    dialog.resizable = true

    val eventNameTF = new TextField()
    val datePicker: DatePicker = new DatePicker()

    val outcomeMap: scala.collection.mutable.Map[Int, TextField] =
      scala.collection.mutable.Map.empty

    var nextOutcomeRow: Int = 2
    val outcomeGrid: GridPane = new GridPane {
      alignment = Pos.Center
      padding = Insets(top = 10, right = 10, bottom = 10, left = 10)
      hgap = 5
      vgap = 5
    }

    def addOutcomeRow(): Unit = {

      val outcomeTF = new TextField()
      val row = nextOutcomeRow
      outcomeMap.addOne((row, outcomeTF))

      outcomeGrid.add(new Label("Potential Outcome"), 0, row)
      outcomeGrid.add(outcomeTF, 1, row)

      nextOutcomeRow += 1
      dialog.dialogPane().getScene.getWindow.sizeToScene()
    }

    addOutcomeRow()
    addOutcomeRow()

    val addOutcomeButton: Button = new Button("+") {
      onAction = _ => addOutcomeRow()
    }

    dialog.dialogPane().content = new VBox() {
      padding = Insets(20, 10, 10, 10)
      spacing = 10
      alignment = Pos.Center

      val eventDataGrid: GridPane = new GridPane {
        padding = Insets(top = 10, right = 10, bottom = 10, left = 10)
        hgap = 5
        vgap = 5

        add(new Label("Event Name"), 0, 0)
        add(eventNameTF, 1, 0)
        add(new Label("Maturity Date"), 0, 1)
        add(datePicker, 1, 1)
      }

      val outcomes: Node = new VBox {
        alignment = Pos.Center

        val label: HBox = new HBox {
          alignment = Pos.Center
          spacing = 10
          children = Vector(new Label("Potential Outcomes"), addOutcomeButton)
        }
        children = Vector(label, outcomeGrid)
      }

      children = Vector(eventDataGrid, new Separator(), outcomes)
    }

    // Enable/Disable OK button depending on whether all data was entered.
    val okButton = dialog.dialogPane().lookupButton(ButtonType.OK)
    // Simple validation that sufficient data was entered
    okButton.disable <== eventNameTF.text.isEmpty

    // When the OK button is clicked, convert the result to a T.
    dialog.resultConverter = dialogButton =>
      if (dialogButton == ButtonType.OK) {
        val eventName = eventNameTF.text.value

        val maturityDate = KrystalBullUtil.toInstant(datePicker)

        val outcomeStrs = outcomeMap.values.toVector.distinct
        val outcomes = outcomeStrs.flatMap { keyStr =>
          if (keyStr.text.value.nonEmpty) {
            Some(keyStr.text.value)
          } else {
            None
          }
        }

        val descriptor = EnumEventDescriptorV0TLV(outcomes)

        val params = InitEventParams(eventName, maturityDate, descriptor)

        Some(params)
      } else None

    dialog.showAndWait() match {
      case Some(Some(params: InitEventParams)) =>
        Some(params)
      case Some(_) | None => None
    }
  }
}
