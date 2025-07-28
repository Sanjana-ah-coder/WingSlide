//Ass1
// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

contract EscrowMarketplace {
    struct Item {
        string name;
        uint256 price;
        address payable seller;
        address buyer;
        bool isSold;
        bool isConfirmed;
    }

    mapping(string => Item) public items; // Unique item name to Item
    mapping(address => string[]) public sellerItems; // Seller to item names
    mapping(address => string[]) public buyerPurchases; // Buyer to item names

    event ItemListed(string itemName, uint256 price, address indexed seller);
    event ItemBought(string itemName, address indexed buyer);
    event Confirmed(string itemName);
    event Refunded(string itemName);

    modifier onlyBuyer(string memory itemName) {
        require(items[itemName].buyer == msg.sender, "Not the buyer");
        _;
    }

    modifier onlySeller(string memory itemName) {
        require(items[itemName].seller == msg.sender, "Not the seller");
        _;
    }

    modifier itemExists(string memory itemName) {
        require(items[itemName].seller != address(0), "Item does not exist");
        _;
    }

    function listItem(string memory itemName, uint256 price) external {
        require(items[itemName].seller == address(0), "Item already listed");
        require(price > 0, "Price must be greater than zero");

        items[itemName] = Item({
            name: itemName,
            price: price,
            seller: payable(msg.sender),
            buyer: address(0),
            isSold: false,
            isConfirmed: false
        });

        sellerItems[msg.sender].push(itemName);
        emit ItemListed(itemName, price, msg.sender);
    }

    function buy(string memory itemName) external payable itemExists(itemName) {
        Item storage item = items[itemName];
        require(!item.isSold, "Item already sold");
        require(msg.value == item.price, "Incorrect amount sent");

        item.buyer = msg.sender;
        item.isSold = true;
        buyerPurchases[msg.sender].push(itemName);

        emit ItemBought(itemName, msg.sender);
    }

    function confirmReceipt(string memory itemName) external onlyBuyer(itemName) itemExists(itemName) {
        Item storage item = items[itemName];
        require(item.isSold, "Item not sold");
        require(!item.isConfirmed, "Already confirmed");

        item.isConfirmed = true;
        item.seller.transfer(item.price);

        emit Confirmed(itemName);
    }

    // Optional: dispute resolution - refund buyer if not confirmed
    function refundBuyer(string memory itemName) external onlySeller(itemName) itemExists(itemName) {
        Item storage item = items[itemName];
        require(item.isSold, "Item not sold");
        require(!item.isConfirmed, "Already confirmed");

        address payable buyer = payable(item.buyer);
        uint256 refundAmount = item.price;

        item.buyer = address(0);
        item.isSold = false;

        buyer.transfer(refundAmount);
        emit Refunded(itemName);
    }

    function getItemDetails(string memory itemName) external view returns (Item memory) {
        return items[itemName];
    }
}

