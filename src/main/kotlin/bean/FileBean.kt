package bean

class FileBean{
    var name: String
    var fold: Boolean
    var size: String
    var parent:String
    var path: String

    constructor(name: String, fold: Boolean, size: String, parent: String){
        this.name = name
        this.fold = fold
        this.size = size
        this.parent = parent
        this.path = "$parent/$name"
    }
}