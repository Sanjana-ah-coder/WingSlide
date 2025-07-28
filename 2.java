
// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

contract DecentralizedVoting {
    struct Voter {
        bool registered;
        bool voted;
        uint256 vote;
        bool blacklisted;
    }

    mapping(address => Voter) public voters;
    mapping(uint256 => uint256) public voteCounts; // candidateId => votes

    address public admin;
    uint256 public registrationDeadline = 1720915200; // 14 July 2024, UNIX timestamp

    event Registered(address voter);
    event Voted(address voter, uint256 candidateId);
    event Blacklisted(address voter);

    modifier onlyBeforeDeadline() {
        require(block.timestamp < registrationDeadline, "Registration closed");
        _;
    }

    modifier onlyRegistered() {
        require(voters[msg.sender].registered, "Not registered");
        _;
    }

    constructor() {
        admin = msg.sender;
    }

    function register() external onlyBeforeDeadline {
        require(!voters[msg.sender].registered, "Already registered");

        voters[msg.sender] = Voter({
            registered: true,
            voted: false,
            vote: 0,
            blacklisted: false
        });

        emit Registered(msg.sender);
    }

    function vote(uint256 candidateId) external onlyRegistered {
        Voter storage voter = voters[msg.sender];

        require(!voter.blacklisted, "Blacklisted");
        
        if (voter.voted) {
            // Remove previous vote and blacklist
            voteCounts[voter.vote]--;
            voter.blacklisted = true;
            voter.voted = false;
            voter.vote = 0;

            emit Blacklisted(msg.sender);
        } else {
            voter.voted = true;
            voter.vote = candidateId;
            voteCounts[candidateId]++;

            emit Voted(msg.sender, candidateId);
        }
    }

    function getVoteCount(uint256 candidateId) external view returns (uint256) {
        return voteCounts[candidateId];
    }

    function isBlacklisted(address voterAddress) external view returns (bool) {
        return voters[voterAddress].blacklisted;
    }
}