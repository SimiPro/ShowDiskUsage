package org.simipro.diskusage.main

import java.io.IOException
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.{FileVisitResult, SimpleFileVisitor, Files, Path}

import scala.collection.mutable

/**
 * Created by Simi on 18.09.2015.
 */
class Calculator {
  var stack:mutable.Stack[TreeFile] = new mutable.Stack[TreeFile]()
  var tree:TreeFile = _



  def buildSystem(rootPath:Path): TreeFile = {
    Files.walkFileTree(rootPath, new SimpleFileVisitor[Path]() {
      override def preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult = {
        stack.push(new TreeFile(null, 0, null))
        FileVisitResult.CONTINUE
      }

      override def visitFileFailed(file: Path, exc: IOException): FileVisitResult = {
        System.err.println("ERR VISITING FILE: " + file.getFileName)
        System.err.println(exc.printStackTrace())
        FileVisitResult.TERMINATE
      }

      override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
        val parent = stack.last
        val node = new TreeFile(file, attrs.size(), parent)
        parent.childs += (node)
        parent.size = parent.size + attrs.size()
        FileVisitResult.CONTINUE
      }

      override def postVisitDirectory(dir: Path, exc: IOException): FileVisitResult = {
        tree = stack.pop()
        FileVisitResult.CONTINUE
      }
    })
    tree
  }


}

class TreeFile(var file:Path, var size:Long, var parent:TreeFile,var childs:mutable.MutableList[TreeFile] = mutable.MutableList[TreeFile]()) {

}
