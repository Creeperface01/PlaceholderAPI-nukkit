import cn.nukkit.Server
import com.creeperface.nukkit.placeholderapi.api.PlaceholderAPI
import com.creeperface.nukkit.placeholderapi.placeholder.StaticPlaceHolder

/**
 * @author CreeperFace
 */
class Test {

    init {
        val api = PlaceholderAPI.getInstance()

        //creating static placeholder
        api.staticPlaceholder("tick", { Server.getInstance().tick }) //the simplest example

        //you can also specify update interval which indicates
        //how long should be value cached until it's recalculated (default is 1 second)

        //PlacholderAPI has also implemented auto updater which updates the placeholder
        //every period specified in update interval

        //autoupdating placehodler

        //this placeholder value will be refreshed every tick and automatically updated
        api.staticPlaceholder("tick", { Server.getInstance().tick }, 1, true)


        //creating visitor sensitive placeholder
        //it's almost the same as static. The only difference is that you can use Player instance parameter in the lambda

        api.visitorSensitivePlaceholder("isop", { p -> p.isOp }) //you can also use update interval and autoupdate parameters

        //every placecholder can have aliases which can be contained in vararg at the end of the method

        //if you want to have better control over the placeholder you can extend existing classes
        //and register Placeholder instance directly like
        api.registerPlaceholder(StaticPlaceHolder("test", 20, false, emptySet()) { "test" })
    }
}