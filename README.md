## CharityChange

### Project idea : ###  
- App that allows you to donate the change in your bank account to selected Charities, and will update News Feed with donations from friends.

**Technologies:** Java | HTML | JS | Spring Framework | MySQL

  - Will use Java to build project. Will create Charity and Clients as clients and MySQL as a database to store and update User information/balance. Sockets will allow chat between Users and Charitiesfor chat, deals, and FAQs.

**API calls to be created on Spring server:**
  - /registerUser - creates User with username and password
  - /login - verify User and return token for other clients to use other API calls
  - /integrateBank - initializes a random balance that would realistically be your balance in the bank
  - /spend - (for purposes of making the app more realistic, this spends random $ amount and updates balance, since I cannot actually connect to bank account transactions)
  - /donate - donates change to a Charity of their choice, updates to server and goes to feed
  - /feed - see who donated to which Charity
  - /sendMessage - sends message to selected Charity and based on message content, Charity will respond with appropriate responses.
  - /subscribe - User can subscribe to certain Charities and will be stored in database.
  - /sendDeals - Charities send message to server that sends to all subscribed Users with deals
  - additions: /loginCharity, /registerCharity, /chat, /updateBalance, /updateBalanceCharity, /quickSearch, /searchSort, /searchSortCharity, /addCharity, /removeCharity

## Database Tables: ##

- **User**

  | username | hashedPassword | hashedBankAccount | balance | donatedTotal |

- **Subscribers**

  | subscriber | charity\_subscribed\_to |

- **Charities**

  | charity | subscribers | donatesTotal |

- **Transactions**

  | user | charity | donatedAmount | timestamp |



**User Flow Diagram**
![E/R Diagram](https://drive.google.com/uc?export=view&id=1Bh66YANxP4inWUcHvDlScWzcxLgu804E)


**TL:DR**

Built a web app that donates change to selected charities using HTML/CSS/JS in the front end, Spring Boot/Maven as the back end, and MySQL as the database.

Implemented a chat function using Socket.io and Threading, set up endpoints using Request Mapping, stored data using JDBC/PreparedStatements, and connected to an Amazon EC2 Instance.

Solo two month final project that utilized Full Stack Technologies with 15 API calls, 4 database entities, 10 HTML pages, and 2 Socket servers.*
