package org.simipro.diskusage.main


import java.net.URI
import java.nio.file._
import java.util.function.Consumer
import javafx.application.Application
import javafx.beans.property.SimpleStringProperty
import javafx.beans.value.{ObservableValue, ChangeListener}

import javafx.collections.{ FXCollections}
import javafx.geometry.Insets

import javafx.scene.Scene
import javafx.scene.control._
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.{GridPane, StackPane}
import javafx.stage.Stage


/**
 * Created by Simi on 18.09.2015.
 */
class ShowDiskUsage extends Application {
  val root = new GridPane()
  val fileGrid = new TableView[SimiPath]
  val fileNamColumn = new TableColumn[SimiPath, String]("File Name")
  val sizeColumn = new TableColumn[SimiPath, String]("Size")
  val files = FXCollections.observableArrayList[SimiPath]()
  buildTable()




  val scene = new Scene(root, 1000, 500)
  val byteToGigabyte = 1024*1024*1024
  var rowIndex = 0



  def addTotalSpace(totalSpace: Long, used:Long, avail:Long) = {
    root.add(new Label("Total Space: "), 2, 0)
    root.add(new Label((totalSpace/byteToGigabyte).toString + " GB"),3,0)
    root.add(new Label("Used Space: "), 2, 1)
    root.add(new Label((used / byteToGigabyte).toString + " GB"),3,1)
    root.add(new Label("Free Space: "), 2, 2)
    root.add(new Label((avail / byteToGigabyte).toString + " GB"),3,2)
    root.requestLayout()
  }


  def buildTable(): Unit = {
    fileNamColumn.setMinWidth(100)
    fileNamColumn.setCellValueFactory(new PropertyValueFactory[SimiPath, String]("fileName"))

    sizeColumn.setMinWidth(100)
    sizeColumn.setCellValueFactory(new PropertyValueFactory[SimiPath, String]("size"))

    fileGrid.getColumns.addAll(fileNamColumn, sizeColumn)
    fileGrid.setItems(files)
  }

  class SimiPath(var fileName:SimpleStringProperty, var size:SimpleStringProperty) {
    def this(name:String, size:String) = this(new SimpleStringProperty(name), new SimpleStringProperty(size))

    def getFileName():String = {
      this.fileName.get()
    }

    def setFileName(name:String): Unit = {
      this.fileName.set(name)
    }


    def getSize():String = {
      this.size.get()
    }

    def setSize(_size:String): Unit = {
      this.size.set(_size)
    }
  }


  override def start(primaryStage: Stage): Unit = {
    val fileSystem = new SimisFileSystem
    fileSystem.build()

    // Chose filesystems
    val fileSystemCombobox = new ComboBox(fileSystem.disks)
    fileSystemCombobox.setPromptText("Choose here")

    // add filegrid
    root.add(fileGrid, 0, 5)


    // Value changed
    fileSystemCombobox.valueProperty().addListener(new ChangeListener[FileStore] {
      override def changed(observable: ObservableValue[_ <: FileStore], oldValue: FileStore, store: FileStore): Unit = {
        addTotalSpace(store.getTotalSpace, store.getTotalSpace - store.getUsableSpace, store.getUsableSpace)
        val disk = getDisk(store)
        val diskAsPath = Paths.get(disk)

        val tree = new Calculator().buildSystem(diskAsPath)
        tree.childs.foreach(T => addFile(T.file))


        /*        Files.walk(diskAsPath, 1).forEach(new Consumer[Path] {
          override def accept(t: Path): Unit = {
              if (t.getFileName != null) {
                addFile(t)
              }
          }
        })
      }
      */
      }
    })


    def addFile(t: Path): Unit = {
      //fileGrid.addRow(rowIndex, new Label(t.getFileName.toString))
     // rowIndex += 1
      files.add(new SimiPath(t.getFileName.toString, (Files.size(t)/(1024*1024)).toString))
    }

    def getDisk(store:FileStore): String = {
      val field = store.getClass.getDeclaredField("root")
      field.setAccessible(true)
      field.get(store).asInstanceOf[String]
    }




   /* val btn = new Button()
    btn.setText("Say Hello World")
    btn.setOnAction(new EventHandler[ActionEvent]() {
      override def handle(event: ActionEvent): Unit = {
        System.out.println("Hello Hello")
      }
    })
    */

    root.setVgap(4)
    root.setHgap(10)
    root.setPadding(new Insets(5, 5, 5, 5))
    root.add(new Label("FileSystem: "), 0,0)
    root.add(fileSystemCombobox, 1, 0)



    primaryStage.setTitle("Hello World")
    primaryStage.setScene(scene)
    primaryStage.show()

  }




}

class SimisFileSystem {
  val disks = FXCollections.observableArrayList[FileStore]()




  def build() = {


    FileSystems.getDefault.getFileStores.forEach(new Consumer[FileStore] {
      override def accept(t: FileStore): Unit =   {
        disks.addAll(t)
        val total = t.getTotalSpace / 1024
        val used = (t.getTotalSpace - t.getUnallocatedSpace) / 1024
        val avail = t.getUsableSpace / 1024
      }
    })
  }

}


object ShowDiskUsageMain   {
  import io.Source._
  import scala.collection.convert._

  def main(args: Array[String]) {
    System.out.println("Write here the disk to check! e.g C, E, D,..")
    //val disk = stdin.getLines().next() + ":"
    val disk = "1"
    System.out.println("You have chosen: " + disk + " good choice!")

    val fileSystem = Paths.get(disk)
    if (!Files.exists(fileSystem)) {
      System.out.println("Please chose a existing Filesystem next time thanks!")
      System.out.println("I just take the default one now.")

    }






   Application.launch(classOf[ShowDiskUsage], args:_*)
  }


}
