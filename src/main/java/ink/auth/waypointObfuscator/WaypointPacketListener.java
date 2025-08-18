package ink.auth.waypointObfuscator;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.world.waypoint.TrackedWaypoint;
import com.github.retrooper.packetevents.util.Either;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWaypoint;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class WaypointPacketListener implements PacketListener {
    private final String secretSalt;

    public WaypointPacketListener(String secretSalt) {
        this.secretSalt = secretSalt == null ? "WaypointObfuscatorDefaultSecret" : secretSalt;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() != PacketType.Play.Server.WAYPOINT) return;

        WrapperPlayServerWaypoint wrapper = new WrapperPlayServerWaypoint(event);
        TrackedWaypoint waypoint = wrapper.getWaypoint();
        if (waypoint == null) return;

        User user = event.getUser();
        UUID viewerUUID = user != null ? user.getUUID() : null;

        Either<UUID, String> identifier = waypoint.getIdentifier();
        UUID left = identifier.getLeft();
        String right = identifier.getRight();

        Either<UUID, String> obfuscatedId;
        if (left != null) {
            UUID obf = obfuscateUUID(left, viewerUUID);
            obfuscatedId = Either.createLeft(obf);
        } else if (right != null) {
            String obfStr = obfuscateString(right, viewerUUID);
            obfuscatedId = Either.createRight(obfStr);
        } else {
            return;
        }

        TrackedWaypoint newWaypoint = new TrackedWaypoint(obfuscatedId, waypoint.getIcon(), waypoint.getInfo());
        wrapper.setWaypoint(newWaypoint);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
    }

    private UUID obfuscateUUID(UUID original, UUID viewer) {
        String seed = secretSalt + "|" + original + "|" + (viewer != null ? viewer : "");
        return UUID.nameUUIDFromBytes(seed.getBytes(StandardCharsets.UTF_8));
    }

    private String obfuscateString(String original, UUID viewer) {
        String seed = secretSalt + "|" + original + "|" + (viewer != null ? viewer : "");
        return UUID.nameUUIDFromBytes(seed.getBytes(StandardCharsets.UTF_8)).toString();
    }
}
