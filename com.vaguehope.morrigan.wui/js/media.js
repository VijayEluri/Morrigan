(function() {

  var DEFAULT_QUERY = '*';

  var mlistsStatusBar;
  var queryStatusBar;

  $(document).ready(function() {
    initStatusBars();
    var mlistsDiv = $('.mlists');
    var mid = UrlParams.params['mid'];
    var pid = UrlParams.params['pid'];
    if (mid) {
      initMlist(mlistsDiv, mid);
      var itemsDiv = $('.mediaitems');
      var search = UrlParams.params['search'];
      search = search ? search : DEFAULT_QUERY;
      runQuery(itemsDiv, mid, search);
    }
    else if (!pid) {
      initMlists(mlistsDiv);
    }
  });

  function initStatusBars() {
    mlistsStatusBar = $('<span>');
    $('.statusbar').append(mlistsStatusBar);
    queryStatusBar = $('<span>');
    $('.statusbar').append(queryStatusBar);
  }

  function initMlists(mlistsDiv) {
    mlistsDiv.empty();
    updateMlists(mlistsDiv);
  }

  function initMlist(mlistsDiv, pid) {
    mlistsDiv.empty();
    updateMlist(mlistsDiv, pid);
  }

  function updateMlists(mlistsDiv) {
    getMlists(function(msg) {
      mlistsStatusBar.text(msg);
    }, function(mlists) {
      $.each(mlists, function(index, mlist) {
        displayMlist(mlistsDiv, mlist, false);
      });
    });
  }

  function updateMlist(mlistsDiv, pid) {
    getMlist(pid, function(msg) {
      mlistsStatusBar.text(msg);
    }, function(mlist) {
      displayMlist(mlistsDiv, mlist, true);
    });
  }

  function displayMlist(mlistsDiv, mlist, detailed) {
    var mlistDivId = 'mlist_' + mlist.id;
    var mlistDiv = $('#' + mlistDivId, mlistsDiv);
    if (mlistDiv.size() < 1) {
      mlistDiv = $('<div class="mlist">');
      mlistDiv.attr('id', mlistDivId);
      makeMlist(mlistDiv, mlist.mid, detailed);
      mlistsDiv.append(mlistDiv);
    }
    updateMlistDisplay(mlistDiv, mlist, detailed);
  }

  function updateMlistDisplay(mlistDiv, mlist, detailed) {
    $('.title', mlistDiv).text(mlist.title);
    if (detailed === true) {
      $('.stats', mlistDiv).text(mlist.count + ' items, ' + (mlist.durationComplete === true ? '' : 'more than ') + mlist.duration + 's.');
    }
  }

  function makeMlist(mlistDiv, mid, detailed) {
    mlistDiv.empty();

    var textBlock = $('<div class="block text">');
    mlistDiv.append(textBlock);

    if (detailed === false) {
      mlistDiv.append($('<a class="details" href="?mid=' + mid + '">'));
    }

    var mainRow = $('<p class="mainrow">');
    textBlock.append(mainRow);
    mainRow.append($('<span class="title">'));

    if (detailed === true) {
      var statsRow = $('<p class="statsrow">');
      textBlock.append(statsRow);
      statsRow.append($('<span class="stats">'));
    }
  }

  function getMlists(onStatus, onMlists) {
    $.ajax({
      type : 'GET',
      cache : false,
      url : 'mlists',
      dataType : 'xml',
      beforeSend : function() {
        onStatus('Reading media lists...');
      },
      success : function(xml) {
        var mlists = [];
        var entires = $(xml).find('entry');
        entires.each(function() {
          var node = $(this);
          var mlist = parseMlistNode(node);
          mlists.push(mlist);
        });
        mlists.sort(function(a, b) {
          return a.mid - b.mid;
        });
        onMlists(mlists);
        onStatus('Media lists updated.');
      },
      error : function(jqXHR, textStatus, errorThrown) {
        onStatus('Error fetching media lists: ' + textStatus);
      }
    });
  }

  function getMlist(mid, onStatus, onMlist) {
    var id = midToId(mid);
    $.ajax({
      type : 'GET',
      cache : false,
      url : 'mlists/' + mid,
      dataType : 'xml',
      beforeSend : function() {
        onStatus('Reading media list ' + id + '...');
      },
      success : function(xml) {
        var mlistNode = $(xml).find('mlist');
        var mlist = parseMlistNode(mlistNode);
        onMlist(mlist);
        onStatus('Media list ' + id + ' updated.');
      },
      error : function(jqXHR, textStatus, errorThrown) {
        onStatus('Error fetching media list ' + id + ': ' + textStatus);
      }
    });
  }

  function parseMlistNode(node) {
    var mlist = {};
    mlist.mid = node.find('link[rel="self"]').attr('href').replace('/mlists/', '');
    mlist.id = midToId(mlist.mid);
    mlist.title = node.find('title').text();
    mlist.count = parseInt(node.find('count').text());
    mlist.duration = parseInt(node.find('duration').text());
    mlist.durationComplete = (node.find('durationcomplete').text() === 'true');
    return mlist;
  }

  function midToId(mid) {
    return mid.match(/\/(.+?)\./)[1];
  }

  function runQuery(itemsDiv, mid, query) {
    itemsDiv.empty();
    itemsDiv.append($('<p>Running query...</p>'));
    updateQuery(itemsDiv, mid, query);
  }

  function updateQuery(itemsDiv, mid, query) {
    getQuery(mid, query, function(msg) {
      queryStatusBar.text(msg);
    }, function(items) {
      displayQuery(itemsDiv, items);
    });
  }

  function displayQuery(itemsDiv, items) {
    itemsDiv.empty();
    $.each(items, function(index, item) {
      itemDiv = $('<div class="item">');
      makeQueryItem(itemDiv);
      updateQueryItemDisplay(itemDiv, item);
      itemsDiv.append(itemDiv);
    });
  }

  function makeQueryItem(itemDiv, id) {
    itemDiv.empty();
    var title = $('<p class="title">');
    var a = $('<a class="clickable" href="">');
    a.append(title);
    itemDiv.append(a);
  }

  function updateQueryItemDisplay(itemDiv, item) {
    $('.title', itemDiv).text(item.title + ' (' + item.duration + 's)');
    var clickable = $('.clickable', itemDiv);
    clickable.unbind();
    clickable.click(function(event) {
      event.preventDefault();
      queryItemClicked(item);
    });
  }

  function getQuery(mid, query, onStatus, onItems) {
    var id = midToId(mid);
    var encodedQuery = encodeURIComponent(query);
    $.ajax({
      type : 'GET',
      cache : false,
      url : 'mlists/' + mid + '/query/' + encodedQuery,
      dataType : 'xml',
      beforeSend : function() {
        onStatus('Querying ' + id + '...');
      },
      success : function(xml) {
        var itemsNode = $(xml).find('mlist');
        var items = parseItemsNode(itemsNode, mid);
        onItems(items);
        onStatus('Query ' + query + ' updated.');
      },
      error : function(jqXHR, textStatus, errorThrown) {
        onStatus('Error querying ' + id + ': ' + textStatus);
      }
    });
  }

  function parseItemsNode(node, mid) {
    var items = [];
    node.find('entry').each(function() {
      var item = parseItemNode($(this), mid);
      items.push(item);
    });
    return items;
  }

  function parseItemNode(node, mid) {
    var item = {};
    item.relativeUrl = node.find('link[rel="self"]').attr('href');
    item.title = node.find('title').text();
    item.duration = parseInt(node.find('duration').text());
    item.startCount = parseInt(node.find('startcount').text());
    item.endCount = parseInt(node.find('endcount').text());

    item.tags = [];
    node.find('tag').each(function() {
      var node = $(this);
      if (node.attr('t') === '0') {
        item.tags.push(node.text());
      }
    });

    item.url = '/mlists/' + mid + '/items/' + item.relativeUrl;

    return item;
  }

  function queryItemClicked(item) {
    var existingMenu = $('.itemmenu');
    if (existingMenu.size() > 0) {
      existingMenu.remove();
    }
    else {
      $('body').append(makeItemMenu(item));
    }
  }

  function makeItemMenu(item) {
    var menu = $('<div class="popup itemmenu">');

    var title = $('<p class="title">');
    title.text(item.title);
    menu.append(title);

    var stats = $('<p class="stats">');
    stats.text(item.startCount + '/' + item.endCount + ' ' + item.duration + 's');
    menu.append(stats);

    var play = $('<button class="play">play</button>');
    menu.append(play);

    var enqueue = $('<button class="enqueue">enqueue</button>');
    menu.append(enqueue);

    var addTag = $('<button class="addtag">add tag</button>');
    addTag.attr('disabled', 'true');
    menu.append(addTag);

    var download = $('<a class="download link">download</a>');
    download.attr('href', item.url);
    menu.append(download);

    var close = $('<button class="close">close</button>');
    menu.append(close);

    play.click(function() {
      play.attr('disabled', 'true');
      enqueue.remove();
      addTag.remove();
      download.remove();
      var status = $('<p>');
      play.after(status);
      chosePlayerAndActionItem(item, 'play', status, function() {
        setTimeout(function() {
          menu.remove();
        }, 1000);
      });
    });

    enqueue.click(function() {
      play.remove();
      enqueue.attr('disabled', 'true');
      addTag.remove();
      download.remove();
      var status = $('<p>');
      enqueue.after(status);
      chosePlayerAndActionItem(item, 'queue', status, function() {
        setTimeout(function() {
          menu.remove();
        }, 1000);
      });
    });

    close.click(function() {
      menu.remove();
    });

    return menu;
  }

  function chosePlayerAndActionItem(item, action, statusElem, onComplete) {
    var onStatus = function(msg) {
      statusElem.text(msg);
    };
    var tpid = UrlParams.params['tpid'];
    if (tpid) {
      actionItem(item, tpid, action, onStatus, onComplete);
    }
    else {
      Players.getPlayers(onStatus, function(players) {
        $.each(players, function(index, player) {
          var play = $('<button>');
          play.text(player.title);
          play.click(function() {
            actionItem(item, player.pid, action, onStatus, onComplete);
          });
          statusElem.after(play);
        });
      });
    }
  }

  function actionItem(item, pid, action, onStatus, onComplete) {
    $.ajax({
      type : 'POST',
      cache : false,
      url : item.url,
      data : 'action=' + action + '&playerid=' + pid,
      contentTypeString : 'application/x-www-form-urlencoded',
      dataType : 'text',
      beforeSend : function() {
        onStatus(action + '-ing...');
      },
      success : function(text) {
        onStatus(text);
        onComplete();
      },
      error : function(jqXHR, textStatus, errorThrown) {
        console.log(jqXHR, textStatus, errorThrown);
        onStatus('Error: ' + textStatus);
      }
    });
  }

})();