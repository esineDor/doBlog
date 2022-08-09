var stompClient = null;


function connect() {
    var socket = new SockJS('/doBlogApplication');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        
        initData();
        
        
        
        
    });
}

function initData() {
	
	var start = stompClient.begin();
	
	 stompClient.send("/app/initData", {transaction: stompClient.id } );
        stompClient.subscribe('/topic/receiveData', function (initData) {
				 
			var statusTextElement = document.createTextNode(JSON.parse(initData.body).content);
			var statusElement = document.getElementById("datastatus");
			statusElement.textContent = "";
			statusElement.appendChild(statusTextElement);
				
			getData();
        });
        
       start.commit();
       
       setInterval(updateBlogs, 5000);
       
    
}

function getData() {
	stompClient.send("/app/getData", {} );
        stompClient.subscribe('/topic/showData', function (sendBlogObjects) {
						
				var obj = JSON.parse(sendBlogObjects.body);
				var i = 0;
				while( i < obj.length ) {
					
					var realObject = obj[i];
					if( realObject === undefined ) {
						continue;
					}
					var objId = realObject.id;
					var content = realObject.content;
					var objLink = realObject.link;
					var objTitle = realObject.blogtitle;
					var objWordCount = JSON.stringify(realObject.wordMapList);
				
					var newElement = createBlogSection( objId, objTitle, content, objLink, objWordCount );
					document.getElementById("overview").appendChild(newElement);

					i = i+1;
			};
        });
}

function createBlogSection( id, title, content, link, wordCountContent ) {
	var sectionElement = document.createElement("section");
	sectionElement.className = "section--center mdl-grid mdl-grid--no-spacing mdl-shadow--2dp";
	sectionElement.id = id;
	
	var cardHeaderdivElement = document.createElement("div");
	cardHeaderdivElement.className = "mdl-card mdl-cell mdl-cell--12-col";
	cardHeaderdivElement.id = id + "_div";
	
	sectionElement.appendChild(cardHeaderdivElement);
	
	//button
	var changeContentButton = document.createElement("button");
	changeContentButton.className = "mdl-button mdl-js-button mdl-js-ripple-effect mdl-button--icon";
	changeContentButton.onclick = function(){changeContent(this.parentNode)};
	
	var changeContentI = document.createElement("i");
	changeContentI.className = "material-icons";
	var textnodei = document.createTextNode("more_vert");
	changeContentI.appendChild(textnodei);	
	
	changeContentButton.appendChild( changeContentI);
	sectionElement.appendChild( changeContentButton );
	
	//First div
	var cardHeaderdivTextElement = document.createElement("div");
	cardHeaderdivTextElement.className = "mdl-card__supporting-text";
	cardHeaderdivTextElement.style.display = "block";
	
	var cardHeaderElement = document.createElement("h4");
	cardHeaderElement.className = "headerBlog_1";
	cardHeaderElement.innerHTML = title;
	
	var cardParagraphElement = document.createElement("p");
	cardParagraphElement.className = "textBlog_1";
	var textnodep = document.createTextNode(content);
	cardParagraphElement.appendChild(textnodep);
	
	cardHeaderdivTextElement.appendChild(cardHeaderElement);
	cardHeaderdivTextElement.appendChild(cardParagraphElement);
	
	cardHeaderdivElement.appendChild( cardHeaderdivTextElement );
	
	//WordContent
	var cardHeaderdivTextElement2 = document.createElement("div");
	cardHeaderdivTextElement2.className = "mdl-card__supporting-text";
	cardHeaderdivTextElement2.style.display = "none";
	
	var cardHeaderElement2 = document.createElement("h4");
	cardHeaderElement2.className = "headerBlog_1";
	cardHeaderElement2.innerHTML = "Wordmap";
	
	var cardParagraphElement2 = document.createElement("p");
	cardParagraphElement2.className = "textBlog_1";
	var textnodep2 = document.createTextNode(wordCountContent);
	cardParagraphElement2.appendChild(textnodep2);
	
	cardHeaderdivTextElement2.appendChild(cardHeaderElement2);
	cardHeaderdivTextElement2.appendChild(cardParagraphElement2);
	
	cardHeaderdivElement.appendChild( cardHeaderdivTextElement2 );
	
	
	
	//second div
	var cardFooterdivTextElement = document.createElement("div");
	cardFooterdivTextElement.className = "mdl-card__actions";
	
	var cardLinkElement = document.createElement("a");
	cardLinkElement.className = "mdl-button";
	cardLinkElement.href = link;
	
	var cardLinkTextElement = document.createTextNode("Mehr erfahren");
	cardLinkElement.appendChild(cardLinkTextElement);
	
	cardFooterdivTextElement.appendChild(cardLinkElement);
	
	cardHeaderdivElement.appendChild( cardFooterdivTextElement );
	
	return sectionElement;
}

function changeContent( section ) {
	
	if( section != null ) {
		var sectiondiv = document.getElementById(section.id + "_div");
		var sectionElements = sectiondiv.children;
		for( var i = 0 ; i < sectionElements.length; i++ ) {
			var sectionElement = sectionElements[i];
			if( sectionElement.style.display == "none") {
				sectionElement.style.display ="block";
			} else if ( sectionElement.style.display == "block") {
				sectionElement.style.display = "none";
			}
		}
	}
}

function disconnect() {
	 if (stompClient !== null) {
        stompClient.disconnect();
    }
    console.log("Disconnected");
}

    
function updateBlogs(){
    
    var transactionUPD = stompClient.begin();
     stompClient.send("/app/update", {transaction: transactionUPD.id } );
        stompClient.subscribe('/topic/updateData', function (updateBlogs) {
				 
			 var response = (JSON.parse(updateBlogs.body).type);
			 if (response == "updated") {
				
				var overviewElement = document.getElementById("overview");
				while (overviewElement.firstChild) {
				    overviewElement.removeChild(overviewElement.lastChild);
				  }
				getData();
				
			} else {
				
				var statusTextElement = document.createTextNode(JSON.parse(updateBlogs.body).content);
			 	var statusElement = document.getElementById("datastatus");
				statusElement.textContent = "";
				statusElement.appendChild(statusTextElement);
			}
				
        });
       
      transactionUPD.commit(); 

   //alert("Hier!");
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    
    connect();
});
