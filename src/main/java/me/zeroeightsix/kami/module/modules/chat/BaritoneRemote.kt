package me.zeroeightsix.kami.module.modules.chat

import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.event.events.PrintChatMessageEvent
import me.zeroeightsix.kami.manager.managers.FriendManager
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Setting
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.event.listener
import me.zeroeightsix.kami.util.text.MessageDetectionHelper
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraft.network.play.server.SPacketChat
import net.minecraft.util.text.TextFormatting

@Module.Info(
        name = "BaritoneRemote",
        description = "Remotely control Baritone with /msg",
        category = Module.Category.CHAT
)
object BaritoneRemote : Module() {
    private val feedback = register(Settings.b("SendFeedback", true))
    private val allow: Setting<Allow> = register(Settings.e("Allow", Allow.FRIENDS))
    private val custom = register(Settings.s("Custom", "unchanged"))

    private var sendNextMsg = false
    private var lastController = "-" /* - is default, ie invalid name */

    init {
        /* instructions for changing custom setting */
        allow.settingListener = Setting.SettingListeners {
            mc.player?.let {
                if ((allow.value == Allow.CUSTOM || allow.value == Allow.FRIENDS_AND_CUSTOM) && custom.value == "unchanged") {
                    MessageSendHelper.sendChatMessage("$chatName Use the &7" + Command.getCommandPrefix()
                            + "set ${name.value} Custom names&f command to change the custom users list. Use , to separate players, for example &7"
                            + Command.getCommandPrefix()
                            + "set ${name.value} Custom dominika,Dewy,086&f")
                }
            }
        }

        /* convert incoming dms into valid baritone commands */
        listener<PacketEvent.Receive> {
            if (it.packet !is SPacketChat) return@listener
            val message = it.packet.getChatComponent().unformattedText

            if (MessageDetectionHelper.isDirect(true, message)) {
                /* side note: this won't work if some glitched account has spaces in their username, but in all honesty, like 3 people globally have those */
                val username = message.split("whispers:")[0].split(" ")[0] // mmmm yes good code
                val command = message.split("whispers:")[1].substring(1)

                if ((!command.startsWith("#") && !command.startsWith(";b ")) || !isValidUser(username)) return@listener

                val baritoneCommand =
                        if (command.startsWith("#")) command.substring(1).split(" ")
                        else command.substring(3).split(" ")

                MessageSendHelper.sendBaritoneCommand(*baritoneCommand.toTypedArray())
                sendNextMsg = true
                lastController = username
            }
        }

        /* forward baritone feedback to controller */
        listener<PrintChatMessageEvent> {
            if (feedback.value && lastController != "-" && it.chatComponent.formattedText.startsWith(TextFormatting.DARK_PURPLE.toString() + "[")) { /* this took like 30 minutes to figure out, fucking Baritone */
                MessageSendHelper.sendServerMessage("/msg $lastController " + it.chatComponent.unformattedText)
            }
        }
    }

    private fun isValidUser(username: String): Boolean {
        return when (allow.value) {
            Allow.ANYBODY -> true
            Allow.FRIENDS -> FriendManager.isFriend(username)
            Allow.CUSTOM -> isCustomUser(username)
            Allow.FRIENDS_AND_CUSTOM -> FriendManager.isFriend(username) || isCustomUser(username)
            else -> false /* never happens */
        }
    }

    private fun isCustomUser(username: String): Boolean {
        val customs = custom.value.split(",")
        for (_custom in customs) {
            if (_custom == username) return true
        }
        return false
    }

    private enum class Allow {
        ANYBODY, FRIENDS, CUSTOM, FRIENDS_AND_CUSTOM
    }
}
