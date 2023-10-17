const users = {
    id: "UUID",
    username: "phihai91",
    email: "phihai91@gmail.com",
    twoMF: false,
    password: "hashing bcrypt",
    registrationDate: 10290392,
    userInfo: {
        firstName: "Hai",
        lastName: "Nguyen",
        avatarUrl: "http://image.png",
    },
    postId: [{
        id: "UUID",
        createdDate: 23123123213,
        status: "PUBLIC"
    }]
}

const posts = {
    id: "UUID",
    userId: "UUID",
    status: "PUBLIC",
    content: "Make me wanna die",
    photoUrls: ["http://image1.png"],
    createdDate: 23123123123,
    comments: ["UUID"],
    likes: ["UUID"]
}

const comments = {
    id: "UUID",
    postId: "UUID",
    userId: "UUID",
    content: "25 Minutes"
}

const likes = {
    id: "UUID",
    postId: "UUID",
    userId: "UUID",
    createdDate: 1022293
}