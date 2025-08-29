package cc.farlanders.votingmatters.api.impl;

import java.util.ArrayList;
import java.util.List;

import cc.farlanders.votingmatters.VotingMatters;
import cc.farlanders.votingmatters.models.VoteRecord;

public class MinecraftMPAPI extends BaseVoteAPI {

    public MinecraftMPAPI(VotingMatters plugin) {
        super(plugin, "minecraftmp");
    }

    @Override
    protected List<VoteRecord> performVoteCheck() {
        return new ArrayList<>();
    }
}
