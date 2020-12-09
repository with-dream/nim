package user;

import java.util.List;

public class GroupResModel extends BaseModel {
    public List<GroupModel> groups;

    @Override
    public String toString() {
        return "FriendResModel{" +
                "groups=" + groups +
                ", code=" + code +
                '}';
    }
}
