package user;

import java.util.List;

public class FriendResModel extends BaseModel {
    public List<FriendModel> friends;

    @Override
    public String toString() {
        return "FriendResModel{" +
                "friends=" + friends +
                ", code=" + code +
                '}';
    }
}
